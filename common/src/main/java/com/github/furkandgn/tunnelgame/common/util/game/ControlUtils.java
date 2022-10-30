package com.github.furkandgn.tunnelgame.common.util.game;

import com.github.furkandgn.tunnelgame.common.game.configuration.ModifiedEntity;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

/**
 * @author Furkan Doğan
 */
public final class ControlUtils {

  public static boolean isClose(ModifiedEntity modifiedEntity, Player player, int activationRange) {

    final LivingEntity livingEntity = (LivingEntity) modifiedEntity.entity();

    final Location entityLocation = livingEntity.getLocation();
    final Location playerLocation = player.getLocation();

    if (!LocationUtil.validWorld(playerLocation.getWorld(), entityLocation.getWorld())) {
      return true;
    }

    return playerLocation.distance(entityLocation) > activationRange;
  }
}
