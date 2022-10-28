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
import me.dantero.tunnelgame.common.manager.InventoryManager;
import me.dantero.tunnelgame.common.manager.PointManager;
import me.dantero.tunnelgame.common.manager.ScoreboardManager;
import me.dantero.tunnelgame.common.manager.WorldManager;
import me.dantero.tunnelgame.common.util.BungeeUtil;
import me.dantero.tunnelgame.common.util.FilenameUtil;
import me.dantero.tunnelgame.common.util.LocationUtil;
import me.dantero.tunnelgame.plugin.level.TunnelLevel;
import me.dantero.tunnelgame.plugin.manager.DefaultInventoryManager;
import me.dantero.tunnelgame.plugin.manager.DefaultScoreboardManager;
import me.dantero.tunnelgame.plugin.menus.UpgradeAffectSelectMenu;
import me.dantero.tunnelgame.plugin.session.manager.MapManager;
import me.dantero.tunnelgame.plugin.session.manager.PlayerInventoryStoreManager;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
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
  private final ScoreboardManager scoreboardManager;
  private final InventoryManager inventoryManager;
  private final PointManager pointManager;

  @Nullable
  private World world;

  public WorldSession(File worldRecoverPath,
                      WorldManager worldManager,
                      LevelConfiguration levelConfiguration,
                      Plugin plugin,
                      PointManager pointManager) {
    Objects.requireNonNull(worldRecoverPath, "World recover path cannot be null");
    Objects.requireNonNull(worldManager, "World manager cannot be null");
    Objects.requireNonNull(levelConfiguration, "Level configuration cannot be null");
    Objects.requireNonNull(plugin, "Plugin cannot be null");
    Objects.requireNonNull(pointManager, "Point manager cannot be null");
    File worldPath = this.buildWorldPath(worldRecoverPath);
    this.sessionId = SESSION_ID_COUNTER.get();
    this.mapManager = new MapManager(worldPath, worldRecoverPath, worldManager);
    this.levelConfiguration = levelConfiguration;
    this.levels = this.buildLevels(levelConfiguration);

    String worldName = this.mapManager.getWorldName();
    PlayerInventoryStoreManager playerInventoryStoreManager = new PlayerInventoryStoreManager();
    this.sessionContext = new DefaultSessionContext(worldName, playerInventoryStoreManager);
    this.entities = new HashSet<>();
    this.currentEntities = new ArrayList<>();

    this.scoreboardManager = new DefaultScoreboardManager(plugin);
    this.scoreboardManager.setup(this);

    this.inventoryManager = new DefaultInventoryManager(() -> this.sessionContext.getPlayers().stream());

    this.pointManager = pointManager;
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
      this.sessionContext.setGameState(GameState.BROKEN);
      return;
    }
    String worldName = this.mapManager.getWorldName();
    this.world = Objects.requireNonNull(Bukkit.getWorld(worldName), "World is null");
    this.prepareLevels();
    this.sessionContext.setGameState(GameState.WAITING);
  }

  @Override
  public void start() {
    this.sessionContext.setGameState(GameState.STARTING);
    this.teleportPlayersSpawn();
    this.inventoryManager.giveStarterKit();
    this.startCountdown(ConfigFile.startCountdown, LanguageFile.gameStarting.build(),
      unused -> {
        this.sessionContext.setGameState(GameState.IN_GAME);
        this.inventoryManager.giveStarterKit();
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
    if (this.world != null) {
      this.world.getPlayers().forEach(player -> player.kickPlayer("Game ended. Thanks for playing."));
    }
    this.sessionContext.clear();
    if (this.sessionContext.isPaused()) {
      this.sessionContext.togglePause();
    }
    this.mapManager.deleteWorld();
  }

  @Override
  public void shutdown() {
    this.stop();
    SESSION_ID_COUNTER.decrementAndGet();
    this.scoreboardManager.stop();
  }

  @Override
  public void restart() {
    this.sessionContext.setGameState(GameState.ROLLBACK);
    this.stop();
    String worldName = this.sessionContext.getWorldName();
    if (Bukkit.getWorld(worldName) != null) {
      Bukkit.unloadWorld(worldName, false);
      this.mapManager.deleteWorld();
    }
    try {
      this.prepare();
    } catch (Exception e) {
      this.sessionContext.setGameState(GameState.BROKEN);
      throw new RuntimeException("An error occurred. Status set to broken.", e);
    }
  }

  @Override
  public void handleLevelPass(Location location) {
    Objects.requireNonNull(Bukkit.getWorld(this.sessionContext.getWorldName()), "World is null")
      .getLivingEntities()
      .stream()
      .filter(livingEntity -> livingEntity instanceof Monster)
      .filter(livingEntity -> this.entities.contains(livingEntity.getEntityId()))
      .peek(livingEntity -> this.entities.remove(livingEntity.getEntityId()))
      .forEach(Entity::remove);

    int maxLevel = this.levels.size();
    int level = this.sessionContext.getCurrentLevel().incrementAndGet();

    if (level > maxLevel) {
      this.peekPlayers(this::sendLobby);
      this.sessionContext.setGameState(GameState.ROLLBACK);
      TaskUtilities.syncLater(20 * 5, bukkitRunnable -> this.restart());
      return;
    }

    this.teleportPlayers(this.currentLevelSpawnPoint());
    this.togglePause();
    this.peekPlayers(player -> UpgradeAffectSelectMenu.open(player, this.sessionContext));
    this.startCountdown(ConfigFile.upgradeCountdown, LanguageFile.waiting4Upgrades.build(), unused -> {
      this.togglePause();
      this.peekPlayers(HumanEntity::closeInventory);
      this.levelConfiguration.levelEntities().get(level).forEach(this::spawnEntity);
      String message = LanguageFile.levelUp.build(Map.entry("%level%", () -> String.valueOf(level)));
      this.sendActionBar(message);
      this.sendTitle("&a", message);
    });
  }

  @Override
  public boolean levelGoalsCompleted() {
    return this.currentEntities.size() == 0;
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
    this.pointManager.clearPoints(player);
    this.sessionContext.handleQuitPlayer(player);
    player.getInventory().clear();
    player.getActivePotionEffects().forEach(potionEffect -> player.removePotionEffect(potionEffect.getType()));
    player.updateInventory();
    player.setGameMode(GameMode.SPECTATOR);
    if (this.maybeEnd()) {
      this.sendLobby(player);
    }
  }

  @Override
  public void handlePlayerRespawn(PlayerRespawnEvent event) {
    Location currentLevelSpawnPoint = this.currentLevelSpawnPoint();
    event.setRespawnLocation(currentLevelSpawnPoint);
    Player player = event.getPlayer();
    this.pointManager.clearPoints(player);
    this.sessionContext.handleQuitPlayer(player);
    player.getInventory().clear();
    player.getActivePotionEffects().forEach(potionEffect -> player.removePotionEffect(potionEffect.getType()));
    player.updateInventory();
    player.setGameMode(GameMode.SPECTATOR);
    this.maybeEnd();
  }

  @Override
  public void handlePlayerQuit(Player player) {
    this.sessionContext.handleQuitPlayer(player);
    player.getActivePotionEffects().forEach(potionEffect -> player.removePotionEffect(potionEffect.getType()));
    player.getInventory().clear();
    if (this.sessionContext.getPlayers().size() == 0 && this.getSessionContext().getGameState() == GameState.IN_GAME) {
      this.sessionContext.setGameState(GameState.ROLLBACK);
      TaskUtilities.syncLater(1, bukkitRunnable -> this.restart());
    }
  }

  @Override
  public JoinResultState tryJoinPlayer(Player player) {
    JoinResultState joinResultState = this.sessionContext.tryJoinPlayer(player);

    if (joinResultState == JoinResultState.SUCCESSFUL) {
      player.getInventory().clear();
      player.updateInventory();
      player.getActivePotionEffects()
        .forEach(potionEffect -> player.removePotionEffect(potionEffect.getType()));
      player.setHealth(20);
      Optional.ofNullable(player.getAttribute(Attribute.GENERIC_MAX_HEALTH))
        .ifPresent(attribute -> attribute.setBaseValue(20));
      player.setFoodLevel(20);
      if (this.sessionContext.getPlayers().size() >= 1 && this.sessionContext.getGameState() == GameState.WAITING) {
        this.start();
      } else {
        Location spawnPoint = ConfigFile.getSpawnPoint();
        spawnPoint.setWorld(Bukkit.getWorld(this.sessionContext.getWorldName()));
        player.teleport(spawnPoint);
      }
    }

    return joinResultState;
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
      vector.multiply(length + 1);
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
      if (this.sessionContext.getGameState() == GameState.ENDED || this.sessionContext.getPlayers().size() == 0) {
        this.sessionContext.setGameState(GameState.WAITING);
        bukkitRunnable.cancel();
        return;
      }

      int countdownInt = countdown.getAndDecrement();
      if (countdownInt <= 0) {
        try {
          onEnd.accept(null);
        } catch (Exception ex) {
          ex.printStackTrace();
        }
        bukkitRunnable.cancel();
      } else {
        String message = XColor.colorize(String.format("&a%s: %s", messagePrefix, countdownInt));
        this.sendTitle("&a", message);
        this.sendActionBar(message);
      }
    });
  }

  private boolean maybeEnd() {
    boolean condition = this.sessionContext.getPlayers().size() == 0 ||
      this.sessionContext.getCurrentLevel().get() >= this.levels.size();
    if (condition) {
      this.sessionContext.setGameState(GameState.ROLLBACK);
      TaskUtilities.syncLater(20 * 5, bukkitRunnable -> this.restart());
    }

    return condition;
  }

  private Location currentLevelSpawnPoint() {
    int currentLevel = this.sessionContext.getCurrentLevel().get();
    int totalLength = this.calculateLevelLength(currentLevel) + currentLevel - 1;
    Location spawnPoint = ConfigFile.getSpawnPoint();
    spawnPoint.setWorld(Objects.requireNonNull(this.world, "World is null"));
    int length = this.levelConfiguration.levelLength().get(currentLevel);
    Vector vector = ConfigFile.gameDirection.getDirection().multiply(totalLength - length);
    return spawnPoint.add(vector);
  }

  private void teleportPlayers(Location location) {
    this.peekPlayers(player -> player.teleport(LocationUtil.randomRotatedLocation(location)));
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

  private void teleportPlayersSpawn() {
    Location spawnPoint = ConfigFile.getSpawnPoint();
    spawnPoint.setWorld(Bukkit.getWorld(this.sessionContext.getWorldName()));
    this.teleportPlayers(spawnPoint);
  }

  private Map<Integer, Level> buildLevels(LevelConfiguration levelConfiguration) {
    return levelConfiguration.levelLength()
      .entrySet()
      .stream()
      .map(entry -> new AbstractMap.SimpleEntry<>(entry.getKey(), new TunnelLevel(entry.getValue(), ConfigFile.lowestBlockHeight)))
      .collect(HashMap::new, (map, entry) -> map.put(entry.getKey(), entry.getValue()), Map::putAll);
  }

  private void sendLobby(Player player) {
    UUID uniqueId = player.getUniqueId();
    String comingFrom = this.sessionContext.getComingFrom(uniqueId);
    if (comingFrom != null && !comingFrom.isBlank()) {
      BungeeUtil.sendPlayerWithoutMessage(player, comingFrom);
    } else {
      player.kickPlayer("Game ended. Thanks for playing.");
    }
  }
}
