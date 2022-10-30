package com.github.furkandgn.tunnelgame.plugin.session;

import com.github.furkandgn.tunnelgame.common.Constants;
import com.github.furkandgn.tunnelgame.common.config.ConfigFile;
import com.github.furkandgn.tunnelgame.common.config.LanguageFile;
import com.github.furkandgn.tunnelgame.common.game.Level;
import com.github.furkandgn.tunnelgame.common.game.Session;
import com.github.furkandgn.tunnelgame.common.game.SessionContext;
import com.github.furkandgn.tunnelgame.common.game.SessionUtil;
import com.github.furkandgn.tunnelgame.common.game.configuration.LevelConfiguration;
import com.github.furkandgn.tunnelgame.common.game.configuration.ModifiedEntity;
import com.github.furkandgn.tunnelgame.common.game.interceptor.EntitySpawnInterceptor;
import com.github.furkandgn.tunnelgame.common.game.state.GameState;
import com.github.furkandgn.tunnelgame.common.game.state.JoinResultState;
import com.github.furkandgn.tunnelgame.common.manager.*;
import com.github.furkandgn.tunnelgame.common.util.game.FilenameUtil;
import com.github.furkandgn.tunnelgame.plugin.manager.DefaultInventoryManager;
import com.github.furkandgn.tunnelgame.plugin.manager.DefaultScoreboardManager;
import com.github.furkandgn.tunnelgame.plugin.menus.UpgradeAffectSelectMenu;
import com.github.furkandgn.tunnelgame.plugin.session.interceptor.DefaultEntitySpawnInterceptor;
import com.github.furkandgn.tunnelgame.plugin.session.manager.DefaultMapManager;
import com.github.furkandgn.tunnelgame.plugin.session.manager.PlayerInventoryStoreManager;
import com.github.furkandgn.tunnelgame.plugin.session.util.DefaultSessionUtil;
import com.gmail.furkanaxx34.dlibrary.bukkit.utils.TaskUtilities;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Furkan Doğan
 */
public class WorldSession implements Session {

  private static final AtomicInteger SESSION_ID_COUNTER = new AtomicInteger(0);

  private final int sessionId;
  private final SessionUtil sessionUtil;
  private final MapManager mapManager;
  private final SessionContext sessionContext;
  private final LevelConfiguration levelConfiguration;
  private final Map<Integer, Level> levels;
  private final Set<Integer> entities;
  private final List<ModifiedEntity> currentEntities;
  private final ScoreboardManager scoreboardManager;
  private final InventoryManager inventoryManager;
  private final PointManager pointManager;

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
    this.entities = new HashSet<>();
    this.currentEntities = new ArrayList<>();
    EntitySpawnInterceptor entitySpawnInterceptor = new DefaultEntitySpawnInterceptor(this.entities, this.currentEntities);

    File worldPath = this.buildWorldPath(worldRecoverPath);
    this.sessionId = SESSION_ID_COUNTER.get();
    this.mapManager = new DefaultMapManager(worldPath, worldRecoverPath, worldManager);
    String worldName = this.mapManager.getWorldName();
    PlayerInventoryStoreManager playerInventoryStoreManager = new PlayerInventoryStoreManager();
    this.sessionContext = new DefaultSessionContext(worldName, playerInventoryStoreManager);
    this.levelConfiguration = levelConfiguration;
    this.sessionUtil = new DefaultSessionUtil(this, entitySpawnInterceptor);
    this.levels = this.sessionUtil.buildLevels();

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
    this.sessionUtil.loadWorld();
    this.sessionUtil.prepareLevels(this.levels);
    this.sessionContext.setGameState(GameState.WAITING);
  }

  @Override
  public void start() {
    this.sessionContext.setGameState(GameState.STARTING);
    this.sessionUtil.teleportPlayersSpawn();
    this.inventoryManager.giveStarterKit();
    this.sessionUtil.startGameCountdown();
  }

  @Override
  public void togglePause() {
    this.sessionContext.togglePause();
  }

  @Override
  public void stop() {
    this.sessionUtil.kickPlayers();
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
    this.sessionUtil.reloadWorld();
  }

  @Override
  public void handleLevelPass(Location location) {
    this.sessionUtil.clearEntities(this.entities);

    int maxLevel = this.levels.size();
    int level = this.sessionContext.getCurrentLevel().incrementAndGet();

    if (level > maxLevel) {
      this.sessionUtil.peekPlayers(this.sessionUtil::sendLobby);
      this.sessionContext.setGameState(GameState.ROLLBACK);
      TaskUtilities.syncLater(20 * 5, bukkitRunnable -> this.restart());
      return;
    }

    this.sessionUtil.teleportPlayers(this.sessionUtil.currentLevelSpawnPoint());
    this.togglePause();
    this.sessionUtil.peekPlayers(player -> UpgradeAffectSelectMenu.open(player, this.sessionContext));
    this.sessionUtil.startCountdown(ConfigFile.upgradeCountdown, LanguageFile.waiting4Upgrades.build(), unused -> {
      this.togglePause();
      this.sessionUtil.peekPlayers(HumanEntity::closeInventory);
      this.levelConfiguration.levelEntities().get(level).forEach(this.sessionUtil::spawnEntity);
      String message = LanguageFile.levelUp.build(Map.entry("%level%", () -> String.valueOf(level)));
      this.sessionUtil.sendActionBar(message);
      this.sessionUtil.sendTitle("&a", message);
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
    this.pointManager.clearPoints(player);
    this.sessionContext.handleQuitPlayer(player);
    player.getInventory().clear();
    player.getActivePotionEffects().forEach(potionEffect -> player.removePotionEffect(potionEffect.getType()));
    player.updateInventory();
    player.setGameMode(GameMode.SPECTATOR);
    if (this.maybeEnd()) {
      this.sessionUtil.sendLobby(player);
    }
  }

  @Override
  public void handlePlayerRespawn(PlayerRespawnEvent event) {
    Location currentLevelSpawnPoint = this.sessionUtil.currentLevelSpawnPoint();
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
        spawnPoint.setWorld(this.sessionUtil.getWorld());
        player.teleport(spawnPoint);
      }
    }

    return joinResultState;
  }

  @Override
  public boolean isInGame(Entity entity) {
    int entityId = entity.getEntityId();
    return this.entities.contains(entityId);
  }

  @Override
  public boolean levelGoalsCompleted() {
    return this.currentEntities.size() == 0;
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
  public Level currentLevel() {
    int currentLevel = this.sessionContext.getCurrentLevel().get();
    return this.levels.get(currentLevel);
  }

  @Override
  public SessionContext getSessionContext() {
    return this.sessionContext;
  }

  @Override
  public List<ModifiedEntity> getModifiedEntities() {
    return Collections.unmodifiableList(this.currentEntities);
  }

  @Override
  public LevelConfiguration getLevelConfiguration() {
    return this.levelConfiguration;
  }

  @Override
  public MapManager getMapManager() {
    return this.mapManager;
  }

  @Override
  public InventoryManager getInventoryManager() {
    return this.inventoryManager;
  }

  private File buildWorldPath(File file) {
    String worldName = FilenameUtil.withoutExtension(file);
    String fileName = String.format("%s-%03d", worldName, SESSION_ID_COUNTER.incrementAndGet());
    return new File(Bukkit.getWorldContainer() + File.separator + "slime_worlds", fileName + ".slime");
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
}
