package me.dantero.tunnelgame.common.game;

import org.jetbrains.annotations.NotNull;

/**
 * @author Furkan Doğan
 */
public interface GameCycle {

  void startGame();

  void stopGame();

  void joinPlayer();

  void leavePlayer();

  @NotNull
  Session getSession();
}