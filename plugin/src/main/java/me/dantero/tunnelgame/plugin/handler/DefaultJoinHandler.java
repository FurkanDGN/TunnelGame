package me.dantero.tunnelgame.plugin.handler;

import me.dantero.tunnelgame.common.game.state.JoinResultState;
import me.dantero.tunnelgame.common.handlers.JoinHandler;
import me.dantero.tunnelgame.common.manager.SessionManager;
import org.bukkit.entity.Player;

/**
 * @author Furkan Doğan
 */
public class DefaultJoinHandler implements JoinHandler {

  private final SessionManager sessionManager;

  public DefaultJoinHandler(SessionManager sessionManager) {
    this.sessionManager = sessionManager;
  }

  @Override
  public void handle(Player player) {
    this.sessionManager.findAvailable()
      .ifPresent(session -> {
        JoinResultState joinResultState = session.tryJoinPlayer(player);
        if (!joinResultState.equals(JoinResultState.SUCCESSFUL)) {
          player.sendMessage("Oyuna giremedin agam");
          return;
        }
        session.start();
      });
  }
}
