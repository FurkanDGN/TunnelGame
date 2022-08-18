package me.dantero.tunnelgame.common.game;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;

/**
 * @author Furkan Doğan
 */
public interface Level {

  void prepare(Location startLocation, BlockFace direction);

  void startLevel();

  void clearLevel();

  int goalLength();

  boolean isStarted();

  boolean isDone();
}
