package me.dantero.tunnelgame.common.misc;

import com.google.protobuf.GeneratedMessageV3;
import me.dantero.tunnelgame.common.proto.GameState;
import me.dantero.tunnelgame.common.proto.NetworkPosition;
import me.dantero.tunnelgame.common.proto.Server;
import me.dantero.tunnelgame.common.proto.ServerMessage;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public interface Protobuf {

  /**
   * creates a server message from the message.
   *
   * @param id the id to create.
   * @param target the target to create.
   * @param message the message to create.
   *
   * @return server message.
   */
  @NotNull
  static ServerMessage createServerMessage(
    @NotNull final String id,
    @NotNull final String target,
    @NotNull final GeneratedMessageV3 message
  ) {
    return ServerMessage.newBuilder()
      .setId(id)
      .setType(message.getClass().getSimpleName())
      .setSource(Servers.getServerName())
      .setTarget(target)
      .setData(message.toByteString())
      .build();
  }

  /**
   * creates a server message from the message.
   *
   * @param target the target to create.
   * @param message the message to create.
   *
   * @return server message.
   */
  @NotNull
  static ServerMessage createServerMessage(
    @NotNull final String target,
    @NotNull final GeneratedMessageV3 message
  ) {
    return Protobuf.createServerMessage(UUID.randomUUID().toString(), target, message);
  }


  /**
   * gets identifier of the server.
   *
   * @param server the server to get.
   *
   * @return server identifier.
   */
  @NotNull
  static String toServerIdentifier(
    @NotNull final Server server
  ) {
    return server.getName();
  }

  /**
   * gets identifier of the server.
   *
   * @param position the position to get.
   *
   * @return server identifier.
   */
  @NotNull
  static String toServerIdentifier(
    @NotNull final NetworkPosition position
  ) {
    return Protobuf.toServerIdentifier(position.getServer());
  }
}