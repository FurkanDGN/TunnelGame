package me.dantero.tunnelgame.plugin.session;

import me.dantero.tunnelgame.common.Constants;
import me.dantero.tunnelgame.common.config.ConfigFile;
import me.dantero.tunnelgame.common.game.SessionContext;
import me.dantero.tunnelgame.common.game.state.GameState;
import me.dantero.tunnelgame.common.game.state.JoinResultState;
import me.dantero.tunnelgame.common.manager.PointManager;
import me.dantero.tunnelgame.common.upgrade.UpgradeType;
import me.dantero.tunnelgame.plugin.manager.DefaultPointManager;
import me.dantero.tunnelgame.plugin.session.manager.PlayerInventoryStoreManager;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author Furkan Doğan
 */
public class DefaultSessionContext implements SessionContext {

  private final PointManager pointManager;
  private final Map<String, Integer> playerUpgrades;
  private final Map<String, Integer> teamUpgrades;
  private final Map<UUID, String> comingFroms;
  private final Set<UUID> players;
  private final String worldName;
  private final PlayerInventoryStoreManager playerInventoryStoreManager;
  private final AtomicInteger currentLevel;
  private final Object stateLock = new Object();
  private final Object upgradeLock = new Object();
  private GameState gameState;
  private boolean paused;

  public DefaultSessionContext(String worldName, PlayerInventoryStoreManager playerInventoryStoreManager) {
    this.pointManager = new DefaultPointManager();
    this.playerUpgrades = new HashMap<>();
    this.teamUpgrades = new HashMap<>();
    this.comingFroms = new HashMap<>();
    this.players = new HashSet<>();
    this.worldName = worldName;
    this.playerInventoryStoreManager = playerInventoryStoreManager;
    this.gameState = GameState.ROLLBACK;
    this.currentLevel = new AtomicInteger(1);
  }

  @Override
  public void upgradePlayer(UUID uniqueId, UpgradeType upgradeType) {
    synchronized (this.upgradeLock) {
      String id = uniqueId.toString();
      String upgradeName = upgradeType.getName();
      String key = Constants.UPGRADES_KEY_FORMAT.formatted(id, upgradeName);
      this.playerUpgrades.compute(key, (s, level) -> level == null ? 1 : level + 1);
    }
  }

  @Override
  public void upgradeTeam(UpgradeType upgradeType) {
    synchronized (this.upgradeLock) {
      String upgradeName = upgradeType.getName();
      this.teamUpgrades.compute(upgradeName, (s, level) -> level == null ? 1 : level + 1);
    }
  }

  @Override
  public int getPlayerUpgrade(UUID uniqueId, UpgradeType upgradeType) {
    synchronized (this.upgradeLock) {
      String upgradeName = upgradeType.getName();
      String key = Constants.UPGRADES_KEY_FORMAT.formatted(uniqueId.toString(), upgradeName);
      return this.playerUpgrades.getOrDefault(key, 0);
    }
  }

  @Override
  public int getTeamUpgrade(UpgradeType upgradeType) {
    synchronized (this.upgradeLock) {
      String upgradeName = upgradeType.getName();
      return this.teamUpgrades.getOrDefault(upgradeName, 0);
    }
  }

  @Override
  public JoinResultState tryJoinPlayer(Player player) {
    UUID uniqueId = player.getUniqueId();

    if (this.players.size() >= ConfigFile.maxPlayers) {
      return JoinResultState.GAME_FULL;
    }
    if (this.isInGame(uniqueId)) {
      return JoinResultState.ALREADY_IN_GAME;
    }

    this.joinPlayer(player);
    return JoinResultState.SUCCESSFUL;
  }

  @Override
  public void handleQuitPlayer(Player player) {
    UUID uniqueId = player.getUniqueId();
    this.players.remove(uniqueId);
  }

  @Override
  public boolean isInGame(UUID uniqueId) {
    return this.players.contains(uniqueId);
  }

  @Override
  public boolean isInGame(Player player) {
    return this.players.contains(player.getUniqueId());
  }

  @Override
  public boolean isStarted() {
    return !this.paused;
  }

  @Override
  public boolean isPaused() {
    return this.paused;
  }

  @Override
  public AtomicInteger getCurrentLevel() {
    return this.currentLevel;
  }

  @Override
  public void togglePause() {
    this.paused = !this.paused;
  }

  @Override
  public void setComingFrom(String uuid, String server) {
    this.comingFroms.put(UUID.fromString(uuid), server);
  }

  @Override
  public String getComingFrom(UUID uuid) {
    return this.comingFroms.get(uuid);
  }

  @Override
  public PointManager getPointManager() {
    return this.pointManager;
  }

  @Override
  public Set<Player> getPlayers() {
    return this.players.stream()
      .map(Bukkit::getPlayer)
      .collect(Collectors.toSet());
  }

  @Override
  public GameState getGameState() {
    synchronized (this.stateLock) {
      return this.gameState;
    }
  }

  @Override
  public void setGameState(GameState gameState) {
    synchronized (this.stateLock) {
      this.gameState = gameState;
    }
  }

  @Override
  public String getWorldName() {
    return this.worldName;
  }

  @Override
  public void clear() {
    this.players.clear();
    this.currentLevel.set(1);
    this.playerUpgrades.clear();
    this.teamUpgrades.clear();
    this.comingFroms.clear();
    this.getPlayers().forEach(player -> {
      player.getInventory().clear();
      player.updateInventory();
      this.playerInventoryStoreManager.resetPlayer(player);
      player.updateInventory();
      player.kickPlayer("Game stopped.");
    });
  }

  private void joinPlayer(Player player) {
    this.players.add(player.getUniqueId());
    this.playerInventoryStoreManager.savePlayer(player);
    player.getInventory().clear();
    player.updateInventory();
    player.setGameMode(GameMode.SURVIVAL);
    player.setHealth(20.0D);
    player.setFoodLevel(20);
  }
}
