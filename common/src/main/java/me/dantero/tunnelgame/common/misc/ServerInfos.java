package me.dantero.tunnelgame.common.misc;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import me.dantero.tunnelgame.common.config.ConfigFile;
import me.dantero.tunnelgame.common.proto.*;
import me.dantero.tunnelgame.common.redis.PubSub;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.*;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author Furkan Doğan
 */
public interface ServerInfos {

  Cache<String, ServerInfo> CACHE = Caffeine.newBuilder()
    .expireAfterWrite(Duration.ofSeconds(3L))
    .build();

  /**
   * finds applicable server.
   *
   * @param current the current to find.
   *
   * @return found server.
   */
  @NotNull
  static Optional<ServerInfo> applicableServerFor(
    final boolean current
  ) {
    if (current) {
      final var server = Servers.instance();
      final var info = ServerInfos.CACHE.getIfPresent(server.name());
      if (info != null) {
        return Optional.of(info);
      }
    }
    Collection<SpigotServer> servers = Servers.LIST;
    final var applicableServers = servers.stream()
      .map(SpigotServer::name)
      .map(ServerInfos.CACHE::getIfPresent)
      .filter(Objects::nonNull)
      .filter(serverInfo -> serverInfo.getSessionsList()
        .stream()
        .anyMatch(session -> session.getPlayerCount() < session.getMaxPlayerCount())
      ).toList();
    if (applicableServers.isEmpty()) {
      return Optional.empty();
    }
    return applicableServers.stream()
      .min(Comparator.comparingInt(value -> value.getSessionsList().stream().min(Comparator.comparingInt(Session::getPlayerCount))
        .orElseThrow().getPlayerCount()));
  }

  /**
   * finds applicable server.
   *
   * @return found server.
   */
  @NotNull
  static Optional<ServerInfo> applicableServerFor() {
    return ServerInfos.applicableServerFor(false);
  }

  static Optional<Session> getSession(String server, int sessionId) {
    return CACHE.asMap().values()
      .stream()
      .filter(serverInfo -> serverInfo.getServer().getName().equals(server))
      .map(ServerInfo::getSessionsList)
      .flatMap(Collection::stream)
      .filter(session -> session.getId() == sessionId)
      .findFirst();
  }

  /**
   * creates a server info.
   *
   * @param tps the tps to create.
   * @param sessions the sessions to create.
   *
   * @return server info.
   */
  @NotNull
  static ServerInfo createInfo(
    final int tps,
    final Set<Session> sessions
    ) {
    return ServerInfo.newBuilder()
      .setServer(Server.newBuilder()
        .setName(Servers.getServerName())
        .build())
      .addAllSessions(sessions)
      .setTps(tps)
      .build();
  }

  /**
   * initiates the server to listen server info messages.
   *
   */
  static void initLobby() {
    PubSub pubSub = new PubSub(Topics.HEARTBEAT);
    pubSub.subscribe(Heartbeat.getDefaultInstance(), ServerInfos::onHeartbeat);
  }

  /**
   * initiates the server info to listen server info messages.
   *
   * @param pubSub the pub sub to listen.
   * @param infoSupplier the info supplier.
   *
   * @return the heartbeat sender.
   */
  @NotNull
  static Runnable init(
    @NotNull final PubSub pubSub,
    @NotNull final Supplier<ServerInfo> infoSupplier
  ) {
    return () -> pubSub.send("*", Heartbeat.newBuilder()
      .setInfo(infoSupplier.get())
      .build());
  }

  /**
   * initiates the server info to listen server info messages.
   *
   * @param infoSupplier the info supplier.
   *
   * @return the heartbeat sender.
   */
  @NotNull
  static Runnable init(
    @NotNull final Supplier<ServerInfo> infoSupplier
  ) {
    return ServerInfos.init(new PubSub(Topics.HEARTBEAT), infoSupplier);
  }

  /**
   * initiates the server info to listen server info messages.
   *
   * @param tpsSupplier the player count supplier to init.
   * @param sessionSupplier the sessions to create.
   *
   * @return the heartbeat sender.
   */
  @NotNull
  static Runnable init(
    @NotNull final IntSupplier tpsSupplier,
    @NotNull final Supplier<Set<me.dantero.tunnelgame.common.game.Session>> sessionSupplier
  ) {
    return ServerInfos.init(() -> ServerInfos.createInfo(tpsSupplier.getAsInt(), buildSessions(sessionSupplier.get())));
  }

  /**
   * converts game session objects to protobuf
   * @param sessionSupplier session supplier to convert.
   * @return protobuf session set.
   */
  private static Set<Session> buildSessions(Set<me.dantero.tunnelgame.common.game.Session> sessionSupplier) {
    return sessionSupplier
      .stream()
      .map(session -> Session.newBuilder()
        .setId(session.sessionId())
        .setGameState(Protobuf.toGameState(session.getSessionContext().getGameState()))
        .setPlayerCount(session.getSessionContext().getPlayers().size())
        .setMaxPlayerCount(ConfigFile.maxPlayers)
        .build())
      .collect(Collectors.toSet());
  }

  /**
   * runs when a server info heartbeats.
   *
   * @param heartbeat the heartbeat to run.
   */
  private static void onHeartbeat(
    @NotNull final Heartbeat heartbeat
  ) {
    final var info = heartbeat.getInfo();
    String name = info.getServer().getName();
    ServerInfos.CACHE.put(name, info);

    Set<Integer> sessions = info.getSessionsList()
      .stream()
      .map(Session::getId)
      .collect(Collectors.toSet());
    SpigotServer server = new SpigotServer(name, sessions);
    Servers.register(server);
  }
}
