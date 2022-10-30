package com.github.furkandgn.tunnelgame.common.game;

import com.github.furkandgn.tunnelgame.common.game.state.GameState;
import com.github.furkandgn.tunnelgame.common.game.state.JoinResultState;
import com.github.furkandgn.tunnelgame.common.manager.PointManager;
import com.github.furkandgn.tunnelgame.common.upgrade.UpgradeType;
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

  void setComingFrom(String uuid, String server);

  String getComingFrom(UUID uuid);

  PointManager getPointManager();

  GameState getGameState();

  void setGameState(GameState gameState);

  Set<Player> getPlayers();

  String getWorldName();

  void clear();
}
