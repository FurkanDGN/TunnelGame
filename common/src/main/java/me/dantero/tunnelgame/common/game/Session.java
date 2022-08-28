package me.dantero.tunnelgame.common.game;

import me.dantero.tunnelgame.common.game.state.JoinResultState;
import org.bukkit.entity.Player;

/**
 * @author Furkan Doğan
 */
public interface Session {

  SessionContext getSessionContext();

  void prepare();

  void start();

  void togglePause();

  void stop();

  void handleLevelPass();

  JoinResultState tryJoinPlayer(Player player);
}