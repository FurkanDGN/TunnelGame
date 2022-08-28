package me.dantero.tunnelgame.common.game;

import me.dantero.tunnelgame.common.game.state.GameState;
import me.dantero.tunnelgame.common.game.state.JoinResultState;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Furkan Doğan
 */
public interface SessionContext {

  void upgradePlayer(UUID uniqueId, String upgradeKey, int level);

  void upgradeTeam(String team, String upgradeKey, int level);

  GameState getGameState();

  JoinResultState tryJoinPlayer(Player player);

  boolean isInGame(UUID uniqueId);
  boolean isInGame(Player player);

  boolean isStarted();

  boolean isPaused();

  AtomicInteger getCurrentLevel();

  void togglePause();

  Set<Player> getPlayers();

  void setGameState(GameState gameState);

  String getWorldName();

  void clear();
}
