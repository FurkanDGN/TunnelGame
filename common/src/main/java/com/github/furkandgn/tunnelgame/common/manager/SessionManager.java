package com.github.furkandgn.tunnelgame.common.manager;

import com.github.furkandgn.tunnelgame.common.game.Session;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
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

  boolean isInGame(LivingEntity livingEntity);

  boolean isSessionWorld(String worldName);

  Optional<Session> getSession(Player player);

  Optional<Session> getSession(Entity livingEntity);

  Optional<Session> getSession(String worldName);

  Optional<Session> findAvailable();

  void clearEndedSessions();
}
