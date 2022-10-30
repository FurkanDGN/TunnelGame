package com.github.furkandgn.tunnelgame.common.upgrade;

/**
 * @author Furkan Doğan
 */
public enum UpgradeType {

  ARMOR("armor"),
  FOOD("food"),
  WEAPON("weapon"),
  SHARPNESS("sharpness"),
  PROTECTION("protection");

  private final String name;

  UpgradeType(String name) {
    this.name = name;
  }

  public String getName() {
    return this.name;
  }
}
