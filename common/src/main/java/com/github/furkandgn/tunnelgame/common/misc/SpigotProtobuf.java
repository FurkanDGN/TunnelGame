package com.github.furkandgn.tunnelgame.common.misc;

import com.github.furkandgn.tunnelgame.common.proto.Earth;
import com.github.furkandgn.tunnelgame.common.proto.Position;
import com.github.furkandgn.tunnelgame.common.proto.User;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * @author Furkan Doğan
 */
public interface SpigotProtobuf {

  /**
   * converts the position to {@link Location}.
   *
   * @param position the position to convert.
   *
   * @return location.
   */
  @NotNull
  static Location toLocation(
    @NotNull final Position position
  ) {
    final var worldName = position.getEarth().getName();
    final var world = Objects.requireNonNull(Bukkit.getWorld(worldName),
      String.format("World called '%s' not found!", worldName));
    return new Location(world, position.getX(), position.getY(), position.getZ(), position.getYaw(), position.getPitch());
  }

  /**
   * converts the location to {@link Position}.
   *
   * @param location the location to convert.
   *
   * @return position.
   */
  @NotNull
  static Position toPosition(
    @NotNull final Location location
  ) {
    final var world = location.getWorld();
    return Position.newBuilder()
      .setEarth(Earth.newBuilder()
        .setId(world.getUID().toString())
        .setName(world.getName())
        .build())
      .setX(location.getX())
      .setY(location.getY())
      .setZ(location.getZ())
      .setYaw(location.getYaw())
      .setPitch(location.getPitch())
      .build();
  }

  /**
   * converts the player to {@link User}.
   *
   * @param player the player to convert.
   *
   * @return user.
   */
  @NotNull
  static User toUser(
    @NotNull final OfflinePlayer player
  ) {
    return User.newBuilder()
      .setId(player.getUniqueId().toString())
      .setName(player.getName())
      .build();
  }
}
