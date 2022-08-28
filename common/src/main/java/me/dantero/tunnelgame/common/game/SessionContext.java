package me.dantero.tunnelgame.common.game;

import me.dantero.tunnelgame.common.game.state.GameState;
import me.dantero.tunnelgame.common.game.state.JoinResultState;
import me.dantero.tunnelgame.common.upgrade.Upgrade;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Furkan Doğan
 */
public interface SessionContext {

  void upgradePlayer(UUID uniqueId, Upgrade upgrade, int level);

  void upgradeTeam(Upgrade upgrade, int level);

  int getPlayerUpgrade(UUID uniqueId, Upgrade upgrade);

  int getTeamUpgrade(Upgrade upgrade);

  JoinResultState tryJoinPlayer(Player player);

  boolean isInGame(UUID uniqueId);

  boolean isInGame(Player player);

  boolean isStarted();

  boolean isPaused();

  AtomicInteger getCurrentLevel();

  void togglePause();

  GameState getGameState();

  void setGameState(GameState gameState);

  Set<Player> getPlayers();

  String getWorldName();

  void clear();
}
