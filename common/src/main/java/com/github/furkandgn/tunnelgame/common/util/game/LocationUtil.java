package com.github.furkandgn.tunnelgame.common.util.game;

import com.github.furkandgn.tunnelgame.common.Constants;
import com.github.furkandgn.tunnelgame.common.config.ConfigFile;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public final class LocationUtil {

  public static Location randomAccessibleLocation(final @NotNull Location baseLocation) {
    final double x = baseLocation.getX() + RandomUtil.randomIntInRange(-Constants.SPAWN_RANGE, Constants.SPAWN_RANGE);
    final double z = baseLocation.getZ() + RandomUtil.randomIntInRange(-Constants.SPAWN_RANGE, Constants.SPAWN_RANGE);

    Location result = baseLocation.getWorld().getHighestBlockAt((int) x, (int) z).getLocation();
    if (!isValid(result)) {
      result = randomAccessibleLocation(baseLocation);
    }

    return result;
  }

  public static Location randomRotatedLocation(final @NotNull Location baseLocation) {
    BlockFace gameDirection = ConfigFile.gameDirection;
    Vector randomVector = gameDirection.getDirection().multiply(com.gmail.furkanaxx34.dlibrary.bukkit.location.RandomUtil.RANDOM.nextDouble(4));
    randomVector.add(LocationUtil.add90Degree(gameDirection).getDirection().multiply(com.gmail.furkanaxx34.dlibrary.bukkit.location.RandomUtil.RANDOM.nextDouble(3)));
    randomVector.add(LocationUtil.sub90Degree(gameDirection).getDirection().multiply(com.gmail.furkanaxx34.dlibrary.bukkit.location.RandomUtil.RANDOM.nextDouble(3)));
    return baseLocation.clone().add(randomVector);
  }

  public static boolean validWorld(final World origin, final World target) {
    return origin.getUID().equals(target.getUID());
  }

  public static double distance(Location first, Location other) {
    return validWorld(first.getWorld(), other.getWorld()) ? first.distance(other) : -1;
  }

  public static boolean isValid(final Location location) {
    return location.getBlock().getType() != Material.AIR
      && location.clone().add(0, 2, 0).getBlock().getType() == Material.AIR;
  }

  public static int getHighestY(final Location location) {
    return location.getWorld().getHighestBlockAt(location).getY();
  }

  public static boolean isSameLocation(Location now, Location last, boolean ignoreY) {
    Location nowLocation = now.getBlock().getLocation();
    Location lastLocation = last.getBlock().getLocation();

    if (ignoreY) {
      nowLocation.setY(0);
      lastLocation.setY(0);
    }

    return nowLocation.equals(lastLocation);
  }

  public static boolean isDifferentWorld(Location now, Location last) {
    return !now.getWorld().getName().equals(last.getWorld().getName());
  }

  public static BlockFace add90Degree(BlockFace blockFace) {
    return switch (blockFace) {
      case NORTH -> BlockFace.EAST;
      case EAST -> BlockFace.SOUTH;
      case SOUTH -> BlockFace.WEST;
      case WEST -> BlockFace.NORTH;
      default -> throw new IllegalArgumentException("Unsupported block face");
    };
  }

  public static BlockFace sub90Degree(BlockFace blockFace) {
    return switch (blockFace) {
      case NORTH -> BlockFace.WEST;
      case EAST -> BlockFace.NORTH;
      case SOUTH -> BlockFace.EAST;
      case WEST -> BlockFace.SOUTH;
      default -> throw new IllegalArgumentException("Unsupported block face");
    };
  }

}
