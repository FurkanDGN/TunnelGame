package com.github.furkandgn.tunnelgame.common.game;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;

/**
 * @author Furkan Doğan
 */
public interface Level {

  void prepare(Location startLocation, BlockFace direction);

  boolean isOutBackside(Location location);

  boolean isPassed(Location location);

  void startLevel();

  void clearLevel();

  int goalLength();

  boolean isStarted();

  boolean isDone();
}
