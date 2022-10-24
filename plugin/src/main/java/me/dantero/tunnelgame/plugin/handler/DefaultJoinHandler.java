package me.dantero.tunnelgame.plugin.handler;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import me.dantero.tunnelgame.common.config.ConfigFile;
import me.dantero.tunnelgame.common.handlers.JoinHandler;
import me.dantero.tunnelgame.common.manager.SessionManager;
import me.dantero.tunnelgame.common.proto.JoinRequest;
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
  public void handleRequest(JoinRequest joinRequest) {
    int sessionId = joinRequest.getSessionId();
    String uuid = joinRequest.getUser().getId();

    this.sessionManager.sessions().stream()
      .filter(session -> session.sessionId() == sessionId)
      .findFirst()
      .ifPresent(session -> {
        String worldName = session.getSessionContext().getWorldName();
        Location spawnPoint = ConfigFile.getSpawnPoint();
        spawnPoint.setWorld(Bukkit.getWorld(worldName));
        this.CACHE.put(uuid, spawnPoint);
      });
  }
}
