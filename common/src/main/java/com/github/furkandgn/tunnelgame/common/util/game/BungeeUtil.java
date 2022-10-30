package com.github.furkandgn.tunnelgame.common.util.game;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import lombok.experimental.UtilityClass;
import com.github.furkandgn.tunnelgame.common.misc.Protobuf;
import com.github.furkandgn.tunnelgame.common.redis.PubSub;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * @author Furkan Doğan
 */
@UtilityClass
public class BungeeUtil {

  @Nullable
  Plugin plugin;

  @Nullable
  PubSub pubSub;

  public void init(Plugin plugin, PubSub pubSub) {
    BungeeUtil.plugin = plugin;
    BungeeUtil.pubSub = pubSub;
  }

  public void sendPlayerWithMessage(Player player, String server, int sessionId) {
    Objects.requireNonNull(plugin, "initiate first.");
    Objects.requireNonNull(pubSub, "initiate first.");

    pubSub.send(Protobuf.createServerMessage(server, Protobuf.toJoinRequest(player, server, sessionId)));

    ByteArrayDataOutput out = ByteStreams.newDataOutput();
    out.writeUTF("Connect");
    out.writeUTF(server);

    player.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
  }

  public void sendPlayerWithoutMessage(Player player, String server) {
    Objects.requireNonNull(plugin, "initiate first.");

    ByteArrayDataOutput out = ByteStreams.newDataOutput();
    out.writeUTF("Connect");
    out.writeUTF(server);

    player.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
  }
}
