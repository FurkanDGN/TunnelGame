package me.dantero.tunnelgame.common.config.pojo;

/**
 * @author Furkan Doğan
 */
public record UpgradeConfig(int requiredPoints, Object object) {

  public boolean has(int requiredPoints) {
    return this.requiredPoints >= requiredPoints;
  }
}
