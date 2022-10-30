package com.github.furkandgn.tunnelgame.common.misc;

import com.google.protobuf.GeneratedMessageV3;
import com.github.furkandgn.tunnelgame.common.proto.*;
import org.bukkit.entity.Player;
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
   * creates a game state from game state.
   * @param gameState real game state object.
   * @return protobuf game state object.
   */
  @NotNull
  static GameState toGameState(com.github.furkandgn.tunnelgame.common.game.state.GameState gameState) {
    return switch (gameState) {
      case WAITING -> GameState.GAME_STATE_WAITING;
      case STARTING -> GameState.GAME_STATE_STARTING;
      case IN_GAME -> GameState.GAME_STATE_IN_GAME;
      case ENDED -> GameState.GAME_STATE_ENDED;
      case ROLLBACK -> GameState.GAME_STATE_ROLLBACK;
      case BROKEN -> GameState.GAME_STATE_BROKEN;
    };
  }

  @NotNull
  static JoinRequest toJoinRequest(Player player, String server, int sessionId) {
    return JoinRequest.newBuilder()
      .setServer(toServer(server))
      .setUser(SpigotProtobuf.toUser(player))
      .setSessionId(sessionId)
      .build();
  }

  @NotNull
  static Server toServer(String server) {
    return Server.newBuilder()
      .setName(server)
      .build();
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