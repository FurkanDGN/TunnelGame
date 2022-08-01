package me.dantero.tunnelgame.common.game;

import me.dantero.tunnelgame.common.game.state.JoinResultState;
import org.bukkit.entity.Player;

import java.util.Set;

/**
 * @author Furkan Doğan
 */
public interface Session {

  void prepare();

  void start();

  void stop();

  JoinResultState tryJoinPlayer(Player player);

  boolean isInGame(Player player);

  boolean isStarted();

  Set<Player> players();

}
