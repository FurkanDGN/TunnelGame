package me.dantero.tunnelgame.common.manager;

import me.dantero.tunnelgame.common.game.Session;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.Set;

/**
 * @author Furkan Doğan
 */
public interface SessionManager {

  void setupSession(Session session);

  Set<Session> sessions();

  boolean isInGame(Player player);

  Optional<Session> getSession(Player player);
}
