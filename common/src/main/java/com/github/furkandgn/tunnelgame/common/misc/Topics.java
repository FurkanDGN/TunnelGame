package com.github.furkandgn.tunnelgame.common.misc;

import java.nio.charset.StandardCharsets;

public interface Topics {

  /**
   * the any topic.
   */
  byte[] ANY = "TunnelGame:*".getBytes(StandardCharsets.UTF_8);

  byte[] JOIN_PLAYER = "TunnelGame:JoinPlayer".getBytes(StandardCharsets.UTF_8);

  /**
   * the heartbeat.
   */
  byte[] HEARTBEAT = "TunnelGame:Heartbeat".getBytes(StandardCharsets.UTF_8);
}
