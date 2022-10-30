// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: definitions.proto

package com.github.furkandgn.tunnelgame.common.proto;

public interface SessionOrBuilder extends
    // @@protoc_insertion_point(interface_extends:com.github.furkandgn.tunnelgame.common.proto.Session)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>int32 id = 1;</code>
   * @return The id.
   */
  int getId();

  /**
   * <code>.com.github.furkandgn.tunnelgame.common.proto.GameState game_state = 2;</code>
   * @return The enum numeric value on the wire for gameState.
   */
  int getGameStateValue();
  /**
   * <code>.com.github.furkandgn.tunnelgame.common.proto.GameState game_state = 2;</code>
   * @return The gameState.
   */
  com.github.furkandgn.tunnelgame.common.proto.GameState getGameState();

  /**
   * <code>sint32 player_count = 3;</code>
   * @return The playerCount.
   */
  int getPlayerCount();

  /**
   * <code>sint32 max_player_count = 4;</code>
   * @return The maxPlayerCount.
   */
  int getMaxPlayerCount();
}