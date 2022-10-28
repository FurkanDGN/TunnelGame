package me.dantero.tunnelgame.plugin.handler;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import me.dantero.tunnelgame.common.config.ConfigFile;
import me.dantero.tunnelgame.common.game.SessionContext;
import me.dantero.tunnelgame.common.handlers.JoinHandler;
import me.dantero.tunnelgame.common.manager.SessionManager;
import me.dantero.tunnelgame.common.proto.JoinRequest;
import me.dantero.tunnelgame.common.proto.ServerMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.Optional;

/**
 * @author Furkan Doğan
 */
public class DefaultJoinHandler implements JoinHandler {

  Cache<String, Location> CACHE = Caffeine.newBuilder()
    .expireAfterWrite(Duration.ofSeconds(3L))
    .build();

  private final SessionManager sessionManager;

  public DefaultJoinHandler(SessionManager sessionManager) {
    this.sessionManager = sessionManager;
  }

  @Override
  public void handle(Player player) {
    String uniqueId = player.getUniqueId().toString();
    Location location = this.CACHE.getIfPresent(uniqueId);
    if (location != null) {
      String name = location.getWorld().getName();
      this.sessionManager.getSession(name)
        .map(session -> session.tryJoinPlayer(player));
    }
  }

  @Override
  public Optional<Location> getSpawnLocation(Player player) {
    String uniqueId = player.getUniqueId().toString();
    return Optional.ofNullable(this.CACHE.getIfPresent(uniqueId));
  }

  @Override
  public void handleRequest(ServerMessage serverMessage, JoinRequest joinRequest) {
    String source = serverMessage.getSource();
    int sessionId = joinRequest.getSessionId();
    String uuid = joinRequest.getUser().getId();

    this.sessionManager.sessions().stream()
      .filter(session -> session.sessionId() == sessionId)
      .findFirst()
      .ifPresent(session -> {
        SessionContext sessionContext = session.getSessionContext();

        sessionContext.setComingFrom(uuid, source);
        String worldName = sessionContext.getWorldName();
        Location spawnPoint = ConfigFile.getSpawnPoint();
        spawnPoint.setWorld(Bukkit.getWorld(worldName));
        this.CACHE.put(uuid, spawnPoint);
      });
  }
}
