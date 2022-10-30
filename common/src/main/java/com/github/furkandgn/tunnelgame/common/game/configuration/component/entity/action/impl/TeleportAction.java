package com.github.furkandgn.tunnelgame.common.game.configuration.component.entity.action.impl;

import com.github.furkandgn.tunnelgame.common.Constants;
import com.github.furkandgn.tunnelgame.common.game.configuration.ModifiedEntity;
import com.github.furkandgn.tunnelgame.common.util.game.LocationUtil;
import com.github.furkandgn.tunnelgame.common.util.game.RandomUtil;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

import java.util.List;

/**
 * @author Furkan Doğan
 */
public class TeleportAction extends AbstractAction {

  private final static List<BlockFace> DIRECTIONS = List.of(
    BlockFace.NORTH,
    BlockFace.EAST,
    BlockFace.SOUTH,
    BlockFace.WEST,
    BlockFace.NORTH_EAST, BlockFace.NORTH_WEST,
    BlockFace.SOUTH_EAST, BlockFace.SOUTH_WEST,
    BlockFace.WEST_NORTH_WEST, BlockFace.WEST_SOUTH_WEST,
    BlockFace.NORTH_NORTH_WEST, BlockFace.NORTH_NORTH_EAST,
    BlockFace.EAST_NORTH_EAST, BlockFace.EAST_SOUTH_EAST,
    BlockFace.SOUTH_SOUTH_EAST, BlockFace.SOUTH_SOUTH_WEST
  );

  private int distance;

  @Override
  protected void init(ConfigurationSection configurationSection) {
    this.distance = configurationSection.getInt("distance");
  }

  @Override
  public void onPlayerMove(ModifiedEntity modifiedEntity, PlayerMoveEvent playerMoveEvent) {
    try {
      Entity mob = modifiedEntity.entity();
      Location location = mob.getLocation();
      Location locationRandomDirection = this.getLocationRandomDirection(location, this.distance);
      if (locationRandomDirection == null) return;
      mob.teleport(locationRandomDirection);
    } catch (RuntimeException e) {
      e.printStackTrace();
    }
  }

  @Override
  protected long getPeriod() {
    return Constants.DEFAULT_COOLDOWN;
  }

  private Location getLocationRandomDirection(Location location, int distance) throws RuntimeException {
    int size = DIRECTIONS.size();

    int retryCount = 0;

    while (retryCount < 1000) {
      int index = RandomUtil.randomIntInRange(size);
      int randomDistance = RandomUtil.randomIntInRange(distance);
      BlockFace blockFace = DIRECTIONS.get(index);
      Vector vector = blockFace.getDirection().multiply(randomDistance);
      Location foundedLocation = location.add(vector);
      int highestY = LocationUtil.getHighestY(foundedLocation);

      if (highestY > 0) {
        foundedLocation.setY(highestY + 1);
        return foundedLocation;
      }

      retryCount++;
    }

    return null;
  }
}
