package me.dantero.tunnelgame.plugin.manager;

import me.dantero.tunnelgame.common.config.ConfigFile;
import me.dantero.tunnelgame.common.game.Session;
import me.dantero.tunnelgame.common.game.state.GameState;
import me.dantero.tunnelgame.common.manager.SessionManager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @author Furkan Doğan
 */
public class DefaultSessionManager implements SessionManager {

  private static final Set<Session> SESSIONS = new CopyOnWriteArraySet<>();

  @Override
  public void setupSession(Session session) {
    SESSIONS.add(session);
    session.prepare();
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
  public boolean isInGame(LivingEntity livingEntity) {
    return SESSIONS.stream().anyMatch(session -> session.isInGame(livingEntity));
  }

  @Override
  public boolean isSessionWorld(String worldName) {
    return SESSIONS.stream().anyMatch(session -> worldName.equals(session.getSessionContext().getWorldName()));
  }

  @Override
  public Optional<Session> getSession(String worldName) {
    return SESSIONS.stream().filter(session -> session.getSessionContext().getWorldName().equals(worldName)).findFirst();
  }

  @Override
  public Optional<Session> getSession(Player player) {
    return SESSIONS.stream().filter(session -> session.getSessionContext().isInGame(player)).findFirst();
  }

  @Override
  public Optional<Session> getSession(Entity entity) {
    return SESSIONS.stream().filter(session -> session.isInGame(entity)).findFirst();
  }

  @Override
  public Optional<Session> findAvailable() {
    return SESSIONS.stream()
      .filter(session -> session.getSessionContext().getGameState().equals(GameState.WAITING))
      .filter(session -> session.getSessionContext().getPlayers().size() < ConfigFile.maxPlayers)
      .findFirst();
  }

  @Override
  public void clearEndedSessions() {
    SESSIONS.removeIf(session -> session.getSessionContext().getGameState().equals(GameState.ENDED));
  }
}
