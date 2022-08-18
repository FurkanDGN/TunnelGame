package me.dantero.tunnelgame.plugin.level;

import me.dantero.tunnelgame.common.game.Level;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;

/**
 * @author Furkan Doğan
 */
public class TunnelLevel implements Level {

  private final int goalLength;
  private boolean started;
  private boolean done;

  public TunnelLevel(int goalLength) {
    this.goalLength = goalLength;
  }

  @Override
  public void prepare(Location startLocation, BlockFace direction) {

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

  @Override
  public boolean isStarted() {
    return this.started;
  }

  @Override
  public boolean isDone() {
    return this.done;
  }
}
