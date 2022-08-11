package me.dantero.tunnelgame.common.game.configuration.component.entity.action;

public enum EventType {

  PLAYER_DISTANCE,
  ON_DAMAGE,
  ON_ATTACK,
  ;

  public static EventType of(final String identifier) {
    EventType result = null;

    for (final EventType value : values()) {
      if (!value.name().equalsIgnoreCase(identifier)) continue;

      result = value;
    }

    return result;
  }

}
