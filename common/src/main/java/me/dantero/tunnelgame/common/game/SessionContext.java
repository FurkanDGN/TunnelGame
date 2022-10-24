package me.dantero.tunnelgame.common.game;

import me.dantero.tunnelgame.common.game.state.GameState;
import me.dantero.tunnelgame.common.game.state.JoinResultState;
import me.dantero.tunnelgame.common.manager.PointManager;
import me.dantero.tunnelgame.common.upgrade.UpgradeType;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Furkan Doğan
 */
public interface SessionContext {

  void upgradePlayer(UUID uniqueId, UpgradeType upgradeType);

  void upgradeTeam(UpgradeType upgradeType);

  int getPlayerUpgrade(UUID uniqueId, UpgradeType upgradeType);

  int getTeamUpgrade(UpgradeType upgradeType);

  JoinResultState tryJoinPlayer(Player player);

  void handleQuitPlayer(Player player);

  boolean isInGame(UUID uniqueId);

  boolean isInGame(Player player);

  boolean isStarted();

  boolean isPaused();

  AtomicInteger getCurrentLevel();

  void togglePause();

  PointManager getPointManager();

  GameState getGameState();

  void setGameState(GameState gameState);

  Set<Player> getPlayers();

  String getWorldName();

  void clear();
}
