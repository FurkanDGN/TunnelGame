package me.dantero.tunnelgame.plugin.manager;

import me.dantero.tunnelgame.common.game.Session;
import me.dantero.tunnelgame.common.manager.SessionManager;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * @author Furkan Doğan
 */
public class DefaultSessionManager implements SessionManager {

  private static final Set<Session> SESSIONS = new HashSet<>();

  @Override
  public void setupSession(Session session) {
    SESSIONS.add(session);
  }

  @Override
  public Set<Session> sessions() {
    return Collections.unmodifiableSet(SESSIONS);
  }

  @Override
  public boolean isInGame(Player player) {
    return SESSIONS.stream().anyMatch(session -> session.getSessionContext().isInGame(player));
  }

  @Override
  public Optional<Session> getSession(Player player) {
    return SESSIONS.stream().filter(session -> session.getSessionContext().isInGame(player)).findFirst();
  }
}
