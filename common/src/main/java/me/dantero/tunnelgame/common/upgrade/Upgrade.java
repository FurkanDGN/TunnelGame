package me.dantero.tunnelgame.common.upgrade;

/**
 * @author Furkan Doğan
 */
public enum Upgrade {

  ARMOR("armor"),
  FOOD("food"),
  WEAPON("weapon"),
  SHARPNESS("sharpness"),
  PROTECTION("protection");

  private final String name;

  Upgrade(String name) {
    this.name = name;
  }

  public String getName() {
    return this.name;
  }
}
