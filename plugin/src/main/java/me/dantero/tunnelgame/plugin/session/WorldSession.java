package me.dantero.tunnelgame.plugin.session;

import com.gmail.furkanaxx34.dlibrary.bukkit.location.RandomUtil;
import com.gmail.furkanaxx34.dlibrary.bukkit.utils.TaskUtilities;
import me.dantero.tunnelgame.common.config.ConfigFile;
import me.dantero.tunnelgame.common.game.Session;
import me.dantero.tunnelgame.common.game.SessionContext;
import me.dantero.tunnelgame.common.game.configuration.LevelConfiguration;
import me.dantero.tunnelgame.common.game.configuration.ModifiedEntitySetting;
import me.dantero.tunnelgame.common.game.state.GameState;
import me.dantero.tunnelgame.common.game.state.JoinResultState;
import me.dantero.tunnelgame.common.manager.WorldManager;
import me.dantero.tunnelgame.common.util.FilenameUtil;
import me.dantero.tunnelgame.plugin.session.manager.MapManager;
import me.dantero.tunnelgame.plugin.session.manager.PlayerInventoryStoreManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Furkan Doğan
 */
public class WorldSession implements Session {

  private static final AtomicInteger SESSION_ID = new AtomicInteger(0);

  private final MapManager mapManager;
  private final LevelConfiguration levelConfiguration;
  private final SessionContext sessionContext;

  public WorldSession(File worldRecoverPath,
                      WorldManager worldManager,
                      LevelConfiguration levelConfiguration) {
    File worldPath = this.buildWorldPath(worldRecoverPath);
    this.mapManager = new MapManager(worldPath, worldRecoverPath, worldManager);
    this.levelConfiguration = levelConfiguration;
    String worldName = this.mapManager.getWorldName();
    PlayerInventoryStoreManager playerInventoryStoreManager = new PlayerInventoryStoreManager();
    this.sessionContext = new DefaultSessionContext(worldName, playerInventoryStoreManager);
  }

  @Override
  public void prepare() {
    this.mapManager.reloadMap();
    this.levelConfiguration.levelEntities().get(1).forEach(this::spawnEntity);
  }

  @Override
  public void start() {
    this.sessionContext.setGameState(GameState.STARTING);

    Location spawnPoint = ConfigFile.getSpawnPoint();
    spawnPoint.setWorld(Bukkit.getWorld(this.sessionContext.getWorldName()));
    this.teleportPlayers(spawnPoint);

    this.startCountdown();
  }

  @Override
  public void togglePause() {
    this.sessionContext.togglePause();
  }

  @Override
  public void stop() {
    SESSION_ID.decrementAndGet();
    this.sessionContext.clear();
  }

  @Override
  public void handleLevelPass() {
    Objects.requireNonNull(Bukkit.getWorld(this.sessionContext.getWorldName())).getLivingEntities()
      .stream()
      .filter(livingEntity -> livingEntity instanceof Monster)
      .peek(livingEntity -> livingEntity.setHealth(0))
      .forEach(Entity::remove);

    int level = this.sessionContext.getCurrentLevel().incrementAndGet();
    this.levelConfiguration.levelEntities().get(level).forEach(this::spawnEntity);
  }

  @Override
  public JoinResultState tryJoinPlayer(Player player) {
    return this.sessionContext.tryJoinPlayer(player);
  }

  private File buildWorldPath(File file) {
    String worldName = FilenameUtil.withoutExtension(file);
    String fileName = String.format("%s-%03d", worldName, SESSION_ID.incrementAndGet());
    return new File(Bukkit.getWorldContainer(), fileName);
  }

  private void startCountdown() {
    AtomicInteger countdown = new AtomicInteger(ConfigFile.startCountdown);
    TaskUtilities.syncTimer(20, bukkitRunnable -> {
      if (countdown.getAndDecrement() <= 0) {
        this.sessionContext.setGameState(GameState.IN_GAME);
        bukkitRunnable.cancel();
      }
    });
  }

  @Override
  public SessionContext getSessionContext() {
    return this.sessionContext;
  }

  private void spawnEntity(ModifiedEntitySetting modifiedEntitySetting) {

  }

  private void teleportPlayers(Location location) {
    this.sessionContext.getPlayers()
      .stream()
      .filter(Objects::nonNull)
      .forEach(player -> player.teleport(location.clone().add(RandomUtil.RANDOM.nextDouble(2), 0, RandomUtil.RANDOM.nextDouble(2))));
  }
}
