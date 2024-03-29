// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: definitions.proto

package com.github.furkandgn.tunnelgame.common.proto;

public interface ServerInfoOrBuilder extends
    // @@protoc_insertion_point(interface_extends:com.github.furkandgn.tunnelgame.common.proto.ServerInfo)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>.com.github.furkandgn.tunnelgame.common.proto.Server server = 1;</code>
   * @return Whether the server field is set.
   */
  boolean hasServer();
  /**
   * <code>.com.github.furkandgn.tunnelgame.common.proto.Server server = 1;</code>
   * @return The server.
   */
  com.github.furkandgn.tunnelgame.common.proto.Server getServer();
  /**
   * <code>.com.github.furkandgn.tunnelgame.common.proto.Server server = 1;</code>
   */
  com.github.furkandgn.tunnelgame.common.proto.ServerOrBuilder getServerOrBuilder();

  /**
   * <code>repeated .com.github.furkandgn.tunnelgame.common.proto.Session sessions = 2;</code>
   */
  java.util.List<com.github.furkandgn.tunnelgame.common.proto.Session> 
      getSessionsList();
  /**
   * <code>repeated .com.github.furkandgn.tunnelgame.common.proto.Session sessions = 2;</code>
   */
  com.github.furkandgn.tunnelgame.common.proto.Session getSessions(int index);
  /**
   * <code>repeated .com.github.furkandgn.tunnelgame.common.proto.Session sessions = 2;</code>
   */
  int getSessionsCount();
  /**
   * <code>repeated .com.github.furkandgn.tunnelgame.common.proto.Session sessions = 2;</code>
   */
  java.util.List<? extends com.github.furkandgn.tunnelgame.common.proto.SessionOrBuilder> 
      getSessionsOrBuilderList();
  /**
   * <code>repeated .com.github.furkandgn.tunnelgame.common.proto.Session sessions = 2;</code>
   */
  com.github.furkandgn.tunnelgame.common.proto.SessionOrBuilder getSessionsOrBuilder(
      int index);

  /**
   * <code>double tps = 3;</code>
   * @return The tps.
   */
  double getTps();
}
