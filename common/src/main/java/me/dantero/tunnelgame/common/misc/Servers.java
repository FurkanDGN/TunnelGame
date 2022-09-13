package me.dantero.tunnelgame.common.misc;

import com.google.common.collect.Sets;
import me.dantero.tunnelgame.common.config.ConfigFile;
import me.dantero.tunnelgame.common.game.state.GameState;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Furkan Doğan
 */
public interface Servers {

  /**
   * the servers.
   */
  Collection<SpigotServer> LIST = Sets.newConcurrentHashSet();

  /**
   * the server.
   */
  AtomicReference<SpigotServer> INSTANCE = new AtomicReference<>();

  /**
   * initiates the server.
   */
  static void init() {
    Servers.INSTANCE.set(new SpigotServer(getServerName(), GameState.WAITING));
  }

  /**
   * gets the server.
   *
   * @return server.
   */
  @NotNull
  static SpigotServer instance() {
    return Objects.requireNonNull(Servers.INSTANCE.get(), "server");
  }

  /**
   * get the current server name.
   * @return server name
   */
  @NotNull
  static String getServerName() {
    return String.format("%s-%s", ConfigFile.lobbyMode ? "lobby" : "server", ConfigFile.serverId);
  }


  /**
   * checks if the server name equals to currently running server.
   *
   * @param name the name to check.
   *
   * @return {@code true} if the given server name equals to currently running server.
   */
  static boolean is(@NotNull final String name) {
    if (name.equals("*")) {
      return true;
    }
    final var currentServerIdentifier = Servers.getServerName();
    return currentServerIdentifier.equals(name);
  }

  /**
   * registers the server.
   *
   * @param server the server to register.
   */
  static void register(@NotNull final SpigotServer server) {
    Servers.LIST.add(server);
  }

  /**
   * unregisters the server.
   *
   * @param server the server to unregister.
   */
  static void unregister(@NotNull final SpigotServer server) {
    Servers.LIST.removeIf(s -> s.name().equals(server.name()));
  }
}
