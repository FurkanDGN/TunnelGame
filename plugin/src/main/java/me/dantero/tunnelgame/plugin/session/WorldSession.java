package me.dantero.tunnelgame.plugin.session;

import com.gmail.furkanaxx34.dlibrary.bukkit.color.XColor;
import com.gmail.furkanaxx34.dlibrary.bukkit.location.RandomUtil;
import com.gmail.furkanaxx34.dlibrary.bukkit.utils.TaskUtilities;
import com.grinderwolf.swm.api.exceptions.UnknownWorldException;
import me.dantero.tunnelgame.common.Constants;
import me.dantero.tunnelgame.common.config.ConfigFile;
import me.dantero.tunnelgame.common.config.LanguageFile;
import me.dantero.tunnelgame.common.game.Level;
import me.dantero.tunnelgame.common.game.Session;
import me.dantero.tunnelgame.common.game.SessionContext;
import me.dantero.tunnelgame.common.game.configuration.LevelConfiguration;
import me.dantero.tunnelgame.common.game.configuration.ModifiedEntity;
import me.dantero.tunnelgame.common.game.configuration.ModifiedEntitySetting;
import me.dantero.tunnelgame.common.game.state.GameState;
import me.dantero.tunnelgame.common.game.state.JoinResultState;
import me.dantero.tunnelgame.common.manager.WorldManager;
import me.dantero.tunnelgame.common.util.FilenameUtil;
import me.dantero.tunnelgame.common.util.LocationUtil;
import me.dantero.tunnelgame.plugin.level.TunnelLevel;
import me.dantero.tunnelgame.plugin.menus.UpgradeAffectSelectMenu;
import me.dantero.tunnelgame.plugin.session.manager.MapManager;
import me.dantero.tunnelgame.plugin.session.manager.PlayerInventoryStoreManager;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * @author Furkan Doğan
 */
public class WorldSession implements Session {

  private static final AtomicInteger SESSION_ID_COUNTER = new AtomicInteger(0);

  private final int sessionId;
  private final MapManager mapManager;
  private final SessionContext sessionContext;
  private final LevelConfiguration levelConfiguration;
  private final Map<Integer, Level> levels;
  private final Set<Integer> entities;
  private final List<ModifiedEntity> currentEntities;

  @Nullable
  private World world;

  public WorldSession(File worldRecoverPath,
                      WorldManager worldManager,
                      LevelConfiguration levelConfiguration) {
    Objects.requireNonNull(worldRecoverPath, "World recover path cannot be null");
    Objects.requireNonNull(worldManager, "World manager cannot be null");
    Objects.requireNonNull(levelConfiguration, "Level configuration cannot be null");

    File worldPath = this.buildWorldPath(worldRecoverPath);
    this.sessionId = SESSION_ID_COUNTER.get();
    this.mapManager = new MapManager(worldPath, worldRecoverPath, worldManager);
    this.levelConfiguration = levelConfiguration;
    this.levels = new HashMap<>();
    levelConfiguration.levelLength()
      .forEach((level, length) -> this.levels.put(level, new TunnelLevel(length, ConfigFile.lowestBlockHeight)));

    String worldName = this.mapManager.getWorldName();
    PlayerInventoryStoreManager playerInventoryStoreManager = new PlayerInventoryStoreManager();
    this.sessionContext = new DefaultSessionContext(worldName, playerInventoryStoreManager);
    this.entities = new HashSet<>();
    this.currentEntities = new ArrayList<>();
  }

  @Override
  public int sessionId() {
    return this.sessionId;
  }

  @Override
  public void prepare() {
    try {
      this.mapManager.reloadMap();
    } catch (UnknownWorldException e) {
      this.mapManager.cloneAndLoadWorld();
    } catch (Exception e) {
      e.printStackTrace();
    }
    String worldName = this.mapManager.getWorldName();
    this.world = Objects.requireNonNull(Bukkit.getWorld(worldName), "World is null");
    this.prepareLevels();
  }

  @Override
  public void start() {
    this.sessionContext.setGameState(GameState.STARTING);

    Location spawnPoint = ConfigFile.getSpawnPoint();
    spawnPoint.setWorld(Bukkit.getWorld(this.sessionContext.getWorldName()));
    this.teleportPlayers(spawnPoint);

    this.startCountdown(ConfigFile.startCountdown, LanguageFile.gameStarting.build(),
      unused -> {
        this.sessionContext.setGameState(GameState.IN_GAME);
        this.levelConfiguration.levelEntities().get(1).forEach(this::spawnEntity);
        String message = LanguageFile.gameStarted.build();
        this.sendActionBar(message);
        this.sendTitle("&b", message);
      });
  }

  @Override
  public void togglePause() {
    this.sessionContext.togglePause();
  }

  @Override
  public void stop() {
    SESSION_ID_COUNTER.decrementAndGet();
    this.peekPlayers(player -> player.kickPlayer("Game ended. Thanks for playing."));
    this.sessionContext.clear();
    this.levels.clear();
    this.mapManager.deleteWorld();
  }

  @Override
  public void handleLevelPass(Location location) {
    Objects.requireNonNull(Bukkit.getWorld(this.sessionContext.getWorldName()), "World is null")
      .getLivingEntities()
      .stream()
      .filter(livingEntity -> livingEntity instanceof Monster)
      .filter(livingEntity -> this.entities.contains(livingEntity.getEntityId()))
      .peek(livingEntity -> this.entities.remove(livingEntity.getEntityId()))
      .peek(livingEntity -> this.currentEntities.removeIf(modifiedEntity -> modifiedEntity.getId() == livingEntity.getEntityId()))
      .peek(livingEntity -> livingEntity.setHealth(0))
      .forEach(Entity::remove);

    int maxLevel = this.levels.size();

    int level = this.sessionContext.getCurrentLevel().incrementAndGet();

    if (level > maxLevel) {
      this.peekPlayers(player -> player.kickPlayer("Game ended. Thanks for playing."));
      TaskUtilities.syncLater(20*2, bukkitRunnable -> this.stop());
      return;
    }

    this.teleportPlayers(this.currentLevelSpawnPoint());
    this.sessionContext.togglePause();
    this.peekPlayers(player -> UpgradeAffectSelectMenu.open(player, this.sessionContext));
    this.startCountdown(ConfigFile.upgradeCountdown, LanguageFile.waiting4Upgrades.build(), unused -> {
      this.sessionContext.togglePause();
      this.peekPlayers(HumanEntity::closeInventory);
      this.levelConfiguration.levelEntities().get(level).forEach(this::spawnEntity);
      String message = LanguageFile.gameStarted.build();
      this.sendActionBar(message);
      this.sendTitle("&a", message);
    });
  }

  @Override
  public void handleEntitySpawn(LivingEntity entity) {
    PersistentDataContainer persistentDataContainer = entity.getPersistentDataContainer();
    Integer root = persistentDataContainer.get(Constants.ROOT_KEY, PersistentDataType.INTEGER);
    if (root != null && this.entities.contains(root)) {
      this.entities.add(entity.getEntityId());
    }
  }

  @Override
  public void handleEntityDeath(LivingEntity livingEntity) {
    this.currentEntities.removeIf(modifiedEntity -> modifiedEntity.getId() == livingEntity.getEntityId());
    this.entities.remove(livingEntity.getEntityId());
  }

  @Override
  public void handlePlayerDeath(Player player) {

  }

  @Override
  public void handlePlayerRespawn(PlayerRespawnEvent event) {
    Location currentLevelSpawnPoint = this.currentLevelSpawnPoint();
    event.setRespawnLocation(currentLevelSpawnPoint);
  }

  @Override
  public JoinResultState tryJoinPlayer(Player player) {
    return this.sessionContext.tryJoinPlayer(player);
  }

  @Override
  public Level currentLevel() {
    int currentLevel = this.sessionContext.getCurrentLevel().get();
    return this.levels.get(currentLevel);
  }

  @Override
  public SessionContext getSessionContext() {
    return this.sessionContext;
  }

  @Override
  public boolean isInGame(Entity entity) {
    int entityId = entity.getEntityId();
    return this.entities.contains(entityId);
  }

  @Override
  public Optional<ModifiedEntity> retrieve(Entity entity) {
    return this.currentEntities.stream()
      .filter(modifiedEntity -> entity.getEntityId() == modifiedEntity.getId())
      .findAny();
  }

  @Override
  public Optional<ModifiedEntity> retrieve(int id) {
    return this.currentEntities.stream()
      .filter(modifiedEntity -> id == modifiedEntity.getId())
      .findAny();
  }

  @Override
  public List<ModifiedEntity> getModifiedEntities() {
    return Collections.unmodifiableList(this.currentEntities);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || this.getClass() != o.getClass()) return false;
    WorldSession that = (WorldSession) o;
    return this.sessionId == that.sessionId;
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.sessionId);
  }

  private File buildWorldPath(File file) {
    String worldName = FilenameUtil.withoutExtension(file);
    String fileName = String.format("%s-%03d", worldName, SESSION_ID_COUNTER.incrementAndGet());
    return new File(Bukkit.getWorldContainer() + File.separator + "slime_worlds", fileName + ".slime");
  }

  private void prepareLevels() {
    if (this.world == null) {
      return;
    }

    Location spawnPoint = ConfigFile.getSpawnPoint();
    spawnPoint.setWorld(this.world);
    BlockFace gameDirection = ConfigFile.gameDirection;

    for (Integer order : this.levels.keySet()) {
      Level level = this.levels.get(order);
      level.prepare(spawnPoint, gameDirection);
      int length = level.goalLength();
      Vector vector = gameDirection.getDirection();
      vector.multiply(length+1);
      spawnPoint.add(vector);
    }
  }

  private void spawnEntity(ModifiedEntitySetting modifiedEntitySetting) {
    for (int i = 0; i < modifiedEntitySetting.getCount(); i++) {
      ModifiedEntity modifiedEntity = modifiedEntitySetting.create(this.mapManager.getWorldName());
      int level = this.sessionContext.getCurrentLevel().get();
      int length = this.calculateLevelLength(level);
      int levelLength = this.levels.get(level).goalLength();
      Location spawn = ConfigFile.getSpawnPoint();
      spawn.setWorld(Objects.requireNonNull(this.world, "World is null"));

      BlockFace gameDirection = ConfigFile.gameDirection;
      Vector vector = gameDirection.getDirection();
      Location entitySpawnPoint = spawn.add(vector.clone().multiply(length - levelLength));
      double randomMargin = 10 + RandomUtil.RANDOM.nextDouble(levelLength - 10);
      entitySpawnPoint.add(vector.multiply(randomMargin));

      entitySpawnPoint.getChunk().load();
      Entity entity = modifiedEntity.initiate(entitySpawnPoint);
      int entityId = entity.getEntityId();
      this.entities.add(entityId);
      this.currentEntities.add(modifiedEntity);
    }
  }

  private int calculateLevelLength(int level) {
    if (level <= 1) {
      return this.levelConfiguration.levelLength().getOrDefault(level, -1);
    } else {
      int length = this.levelConfiguration.levelLength().get(level);
      return length + this.calculateLevelLength(level - 1);
    }
  }

  private void startCountdown(int time, String messagePrefix, Consumer<Void> onEnd) {
    AtomicInteger countdown = new AtomicInteger(time);
    TaskUtilities.syncTimer(20, bukkitRunnable -> {
      if (this.sessionContext.getGameState() == GameState.ENDED) {
        bukkitRunnable.cancel();
        return;
      }

      int countdownInt = countdown.getAndDecrement();
      if (countdownInt <= 0) {
        onEnd.accept(null);
        bukkitRunnable.cancel();
      } else {
        String message = XColor.colorize(String.format("&a%s: %s", messagePrefix, countdownInt));
        this.sendTitle("&a", message);
        this.sendActionBar(message);
      }
    });
  }

  private void teleportPlayers(Location location) {
    BlockFace gameDirection = ConfigFile.gameDirection;
    Vector randomVector = gameDirection.getDirection().multiply(RandomUtil.RANDOM.nextDouble(4));
    randomVector.add(LocationUtil.add90Degree(gameDirection).getDirection().multiply(RandomUtil.RANDOM.nextDouble(3)));
    randomVector.add(LocationUtil.sub90Degree(gameDirection).getDirection().multiply(RandomUtil.RANDOM.nextDouble(3)));
    Location newLocation = location.clone().add(randomVector);
    this.peekPlayers(player -> player.teleport(newLocation));
  }

  private void sendTitle(String title, String subtitle) {
    this.peekPlayers(player -> player.sendTitle(XColor.colorize(title), XColor.colorize(subtitle), 15, 20, 3));
  }

  private void sendActionBar(String message) {
    this.peekPlayers(player -> player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message)));
  }

  private void peekPlayers(Consumer<Player> playerConsumer) {
    this.sessionContext.getPlayers()
      .stream()
      .filter(Objects::nonNull)
      .forEach(playerConsumer);
  }

  private Location currentLevelSpawnPoint() {
    int currentLevel = this.sessionContext.getCurrentLevel().get();
    int totalLength = this.calculateLevelLength(currentLevel);
    Location spawnPoint = ConfigFile.getSpawnPoint();
    spawnPoint.setWorld(Objects.requireNonNull(this.world, "World is null"));
    int length = this.levelConfiguration.levelLength().get(currentLevel);
    Vector vector = ConfigFile.gameDirection.getDirection().multiply(totalLength - length);
    return spawnPoint.add(vector);
  }
}
