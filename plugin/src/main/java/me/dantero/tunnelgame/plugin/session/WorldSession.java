package me.dantero.tunnelgame.plugin.session;

import com.gmail.furkanaxx34.dlibrary.bukkit.location.RandomUtil;
import com.gmail.furkanaxx34.dlibrary.bukkit.utils.TaskUtilities;
import me.dantero.tunnelgame.common.config.ConfigFile;
import me.dantero.tunnelgame.common.game.Session;
import me.dantero.tunnelgame.common.game.configuration.LevelConfiguration;
import me.dantero.tunnelgame.common.game.configuration.ModifiedEntitySetting;
import me.dantero.tunnelgame.common.game.state.GameState;
import me.dantero.tunnelgame.common.game.state.JoinResultState;
import me.dantero.tunnelgame.common.manager.WorldManager;
import me.dantero.tunnelgame.common.util.FilenameUtil;
import me.dantero.tunnelgame.plugin.session.manager.MapManager;
import me.dantero.tunnelgame.plugin.session.manager.PlayerInventoryStore;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author Furkan Doğan
 */
public class WorldSession implements Session {

  private static final AtomicInteger SESSION_ID = new AtomicInteger(0);

  private final Set<UUID> players = new HashSet<>();
  private final PlayerInventoryStore playerInventoryStore = new PlayerInventoryStore();
  private final MapManager mapManager;
  private final LevelConfiguration levelConfiguration;
  private final String worldName;
  private GameState gameState = GameState.WAITING;
  private int currentLevel = 1;

  public WorldSession(File worldRecoverPath,
                      WorldManager worldManager,
                      LevelConfiguration levelConfiguration) {
    File worldPath = this.buildWorldPath(worldRecoverPath);
    this.mapManager = new MapManager(worldPath, worldRecoverPath, worldManager);
    this.levelConfiguration = levelConfiguration;
    this.worldName = this.mapManager.getWorldName();
  }

  @Override
  public void prepare() {
    this.mapManager.reloadMap();
    this.levelConfiguration.levelEntities().get(1).forEach(this::spawnEntity);
  }

  @Override
  public void start() {
    this.gameState = GameState.STARTING;

    Location spawnPoint = ConfigFile.getSpawnPoint();
    spawnPoint.setWorld(Bukkit.getWorld(this.worldName));
    this.teleportPlayers(spawnPoint);

    this.startCountdown();
  }

  @Override
  public void stop() {
    SESSION_ID.decrementAndGet();
    this.resetPlayers();
    this.gameState = GameState.WAITING;
  }

  @Override
  public void handleLevelPass() {
    Objects.requireNonNull(Bukkit.getWorld(this.worldName)).getLivingEntities()
      .stream()
      .filter(livingEntity -> livingEntity instanceof Monster)
      .peek(livingEntity -> livingEntity.setHealth(0))
      .forEach(Entity::remove);

    this.levelConfiguration.levelEntities().get(++this.currentLevel).forEach(this::spawnEntity);
  }

  @Override
  public GameState getGameState() {
    return this.gameState;
  }

  @Override
  public JoinResultState tryJoinPlayer(Player player) {
    UUID uniqueId = player.getUniqueId();

    if (this.players.size() >= ConfigFile.maxPlayers) {
      return JoinResultState.GAME_FULL;
    }
    if (this.players.contains(uniqueId)) {
      return JoinResultState.ALREADY_IN_GAME;
    }

    this.joinPlayer(player);
    return JoinResultState.SUCCESSFUL;
  }

  @Override
  public boolean isInGame(Player player) {
    return this.players.contains(player.getUniqueId());
  }

  @Override
  public boolean isStarted() {
    return false;
  }

  @Override
  public Set<Player> players() {
    return this.players.stream()
      .map(Bukkit::getPlayer)
      .collect(Collectors.toSet());
  }

  private void joinPlayer(Player player) {
    this.players.add(player.getUniqueId());
    this.playerInventoryStore.savePlayer(player);
  }

  private File buildWorldPath(File file) {
    String worldName = FilenameUtil.withoutExtension(file);
    String fileName = String.format("%s-%03d", worldName, SESSION_ID.incrementAndGet());
    return new File(Bukkit.getWorldContainer(), fileName);
  }

  private void resetPlayers() {
    this.players.clear();
    this.players().forEach(player -> {
      this.playerInventoryStore.resetPlayer(player);
      player.kickPlayer("Game stopped.");
    });
  }

  private void startCountdown() {
    AtomicInteger countdown = new AtomicInteger(ConfigFile.startCountdown);
    TaskUtilities.syncTimer(20, bukkitRunnable -> {
      if (countdown.getAndDecrement() <= 0) {
        this.gameState = GameState.IN_GAME;
        bukkitRunnable.cancel();
      }
    });
  }

  private void spawnEntity(ModifiedEntitySetting modifiedEntitySetting) {

  }

  private void teleportPlayers(Location location) {
    this.players.stream()
      .map(Bukkit::getPlayer)
      .filter(Objects::nonNull)
      .forEach(player -> player.teleport(location.clone().add(RandomUtil.RANDOM.nextDouble(2), 0, RandomUtil.RANDOM.nextDouble(2))));
  }
}
