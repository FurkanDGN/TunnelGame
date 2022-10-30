package com.github.furkandgn.tunnelgame.common.game.configuration.component.entity.action;

import com.github.furkandgn.tunnelgame.common.game.configuration.component.entity.action.impl.*;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

public enum ActionType {

  EFFECT(EffectAction.class), // DONE
  STEAL(StealAction.class), // DONE
  TELEPORT(TeleportAction.class), // DONE
  SPAWN(SpawnAction.class), // DONE
  CONVERT(ConvertAction.class), // DONE
  REPULSE(RepulseAction.class), // DONE
  LIFE_STEAL(LifeStealAction.class); // DONE

  private final Class<? extends TriggerAction> actionClass;

  ActionType(final Class<? extends TriggerAction> actionClass) {
    this.actionClass = actionClass;
  }

  public static TriggerAction of(final String identifier) throws InvocationTargetException, InstantiationException, IllegalAccessException {
    ActionType resultType = Arrays.stream(values())
      .filter(actionType -> actionType.name().equalsIgnoreCase(identifier))
      .findAny()
      .orElse(null);

    return resultType == null ? null : resultType.newInstance();
  }

  private TriggerAction newInstance() throws InvocationTargetException, InstantiationException, IllegalAccessException {
    return (TriggerAction) this.actionClass.getConstructors()[0].newInstance();
  }

}
