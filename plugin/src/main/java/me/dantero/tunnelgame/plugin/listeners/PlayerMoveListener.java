package me.dantero.tunnelgame.plugin.listeners;

import me.dantero.tunnelgame.common.game.Level;
import me.dantero.tunnelgame.common.game.Listener;
import me.dantero.tunnelgame.common.game.state.GameState;
import me.dantero.tunnelgame.common.manager.SessionManager;
import me.dantero.tunnelgame.common.util.LocationUtil;
import org.bukkit.Location;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.Plugin;

/**
 * @author Furkan Doğan
 */
public class PlayerMoveListener extends Listener {

  public PlayerMoveListener(Plugin plugin, SessionManager sessionManager) {
    super(plugin, sessionManager);
  }

  @Override
  public void register() {
    this.listenInGameEvent(PlayerMoveEvent.class, (event, session) -> {
      Location from = event.getFrom();
      Location to = event.getTo();
      if (to == null) return;

      if (LocationUtil.isSameLocation(to, from, true)) return;

      if (session.getSessionContext().isPaused() || session.getSessionContext().getGameState().equals(GameState.STARTING)) {
        event.setCancelled(true);
        return;
      }

      Level level = session.currentLevel();

      if (level.isOutBackside(to)) {
        event.setCancelled(true);
      } else if (level.isPassed(to)) {
        session.handleLevelPass(to);
      }
    });
  }
}
