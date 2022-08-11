package me.dantero.tunnelgame.common.util;

import me.dantero.tunnelgame.common.Constants;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
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

}
