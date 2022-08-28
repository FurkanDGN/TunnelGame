package me.dantero.tunnelgame.plugin.session;

import me.dantero.tunnelgame.common.config.ConfigFile;
import me.dantero.tunnelgame.common.game.SessionContext;
import me.dantero.tunnelgame.common.game.state.GameState;
import me.dantero.tunnelgame.common.game.state.JoinResultState;
import me.dantero.tunnelgame.plugin.session.manager.PlayerInventoryStoreManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author Furkan Doğan
 */
public class DefaultSessionContext implements SessionContext {

  private final Set<UUID> players;
  private final String worldName;
  private final PlayerInventoryStoreManager playerInventoryStoreManager;
  private final AtomicInteger currentLevel;
  private final Object lock = new Object();
  private GameState gameState;
  private boolean paused;

  public DefaultSessionContext(String worldName, PlayerInventoryStoreManager playerInventoryStoreManager) {
    this.players = new HashSet<>();
    this.worldName = worldName;
    this.playerInventoryStoreManager = playerInventoryStoreManager;
    this.gameState = GameState.WAITING;
    this.currentLevel = new AtomicInteger(1);
  }

  @Override
  public void upgradePlayer(UUID uniqueId, String upgradeKey, int level) {

  }

  @Override
  public void upgradeTeam(String team, String upgradeKey, int level) {

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
  public boolean isInGame(UUID uniqueId) {
    return this.players.contains(uniqueId);
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
  public Set<Player> getPlayers() {
    return this.players.stream()
      .map(Bukkit::getPlayer)
      .collect(Collectors.toSet());
  }

  @Override
  public GameState getGameState() {
    synchronized (this.lock) {
      return this.gameState;
    }
  }

  @Override
  public void setGameState(GameState gameState) {
    synchronized (this.lock) {
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
    this.gameState = GameState.WAITING;
    this.getPlayers().forEach(player -> {
      this.playerInventoryStoreManager.resetPlayer(player);
      player.kickPlayer("Game stopped.");
    });
  }

  private void joinPlayer(Player player) {
    this.players.add(player.getUniqueId());
    this.playerInventoryStoreManager.savePlayer(player);
  }
}
