package com.github.furkandgn.tunnelgame.plugin.level;

import com.github.furkandgn.tunnelgame.common.game.Level;
import com.github.furkandgn.tunnelgame.common.util.game.LocationUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;

import java.util.Objects;

/**
 * @author Furkan Doğan
 */
public class TunnelLevel implements Level {

  private static final Material START_BLOCK_TYPE = Material.GREEN_CONCRETE;
  private static final Material END_BLOCK_TYPE = Material.RED_CONCRETE;

  private final int goalLength;
  private final int blockHeight;
  private boolean started;
  private boolean done;
  private Location startLeftSide;
  private Location startRightSide;
  private Location endLeftSide;
  private Location endRightSide;

  public TunnelLevel(int goalLength, int blockHeight) {
    if (goalLength < 10) {
      throw new IllegalArgumentException("Tunnel level length cannot be less or equal to 10");
    }
    this.goalLength = goalLength;
    this.blockHeight = blockHeight;
  }

  @Override
  public void prepare(Location startLocation, BlockFace direction) {
    BlockFace rightSide = LocationUtil.add90Degree(direction);
    BlockFace leftSide = LocationUtil.sub90Degree(direction);
    // Set START blocks
    Location startLocationClone = startLocation.clone();
    startLocationClone.setY(this.blockHeight);
    Block startBlock = startLocationClone.getBlock();
    Block startRightBlock = startBlock.getLocation().add(rightSide.getDirection().multiply(2)).getBlock();
    Block startLeftBlock = startBlock.getLocation().add(leftSide.getDirection().multiply(2)).getBlock();
    startBlock.setType(START_BLOCK_TYPE);
    startRightBlock.setType(START_BLOCK_TYPE);
    startLeftBlock.setType(START_BLOCK_TYPE);

    // Set END blocks
    Vector vector = direction.getDirection();
    Location endLocation = startLocation.clone().add(vector.multiply(this.goalLength));
    endLocation.setY(this.blockHeight);
    Block endBlock = endLocation.getBlock();
    Block endRightBlock = endBlock.getLocation().add(rightSide.getDirection().multiply(2)).getBlock();
    Block endLeftBlock = endBlock.getLocation().add(leftSide.getDirection().multiply(2)).getBlock();
    endBlock.setType(END_BLOCK_TYPE);
    endRightBlock.setType(END_BLOCK_TYPE);
    endLeftBlock.setType(END_BLOCK_TYPE);

    this.startLeftSide = startLeftBlock.getLocation();
    this.startRightSide = startRightBlock.getLocation();
    this.endLeftSide = endLeftBlock.getLocation();
    this.endRightSide = endRightBlock.getLocation();
  }

  @Override
  public boolean isOutBackside(Location location) {
    this.checkSides();
    if (LocationUtil.isDifferentWorld(location, this.startLeftSide)) return true;

    double distanceToStartLeft = this.startLeftSide.distance(location) - 0.5d;
    double distanceToStartRight = this.startLeftSide.distance(location) - 0.5d;
    double distanceToEndLeft = this.endLeftSide.distance(location) - 0.5d;
    double distanceToEndRight = this.endLeftSide.distance(location) - 0.5d;

    return distanceToEndLeft > this.goalLength && distanceToEndRight > this.goalLength &&
      distanceToStartLeft < this.goalLength && distanceToStartRight < this.goalLength;
  }

  @Override
  public boolean isPassed(Location location) {
    this.checkSides();
    if (LocationUtil.isDifferentWorld(location, this.startLeftSide)) return true;

    double distanceToStartLeft = this.startLeftSide.distance(location);
    double distanceToStartRight = this.startLeftSide.distance(location);
    double distanceToEndLeft = this.endLeftSide.distance(location);
    double distanceToEndRight = this.endLeftSide.distance(location);

    return distanceToStartLeft > this.goalLength && distanceToStartRight > this.goalLength &&
      distanceToEndLeft < this.goalLength && distanceToEndRight < this.goalLength;
  }

  @Override
  public void startLevel() {
    this.started = true;
  }

  @Override
  public void clearLevel() {
    this.started = false;
    this.done = true;
  }

  public int goalLength() {
    return this.goalLength;
  }

  public int getBlockHeight() {
    return this.blockHeight;
  }

  @Override
  public boolean isStarted() {
    return this.started;
  }

  @Override
  public boolean isDone() {
    return this.done;
  }

  private void checkSides() {
    Objects.requireNonNull(this.startLeftSide, "Level has not prepared yet.");
    Objects.requireNonNull(this.startRightSide, "Level has not prepared yet.");
    Objects.requireNonNull(this.endLeftSide, "Level has not prepared yet.");
    Objects.requireNonNull(this.endRightSide, "Level has not prepared yet.");
  }
}
