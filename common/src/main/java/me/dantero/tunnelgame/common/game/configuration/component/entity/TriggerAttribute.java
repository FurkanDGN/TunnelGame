package me.dantero.tunnelgame.common.game.configuration.component.entity;

import me.dantero.tunnelgame.common.game.configuration.ModifiedEntity;
import me.dantero.tunnelgame.common.game.configuration.component.entity.action.ActionType;
import me.dantero.tunnelgame.common.game.configuration.component.entity.action.EventType;
import me.dantero.tunnelgame.common.game.configuration.component.entity.action.TriggerAction;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public final class TriggerAttribute {

  private final List<TriggerAction> triggerActions = new ArrayList<>();

  public TriggerAttribute(final @NotNull ConfigurationSection configuration) {
    for (final String triggerEventKey : configuration.getKeys(false)) {
      final ConfigurationSection triggerEvent = configuration.getConfigurationSection(triggerEventKey);
      if (triggerEvent == null) continue;

      for (final String resultKey : triggerEvent.getKeys(false)) {
        final ConfigurationSection resultSection = triggerEvent.getConfigurationSection(resultKey);
        if (resultSection == null) continue;

        for (final String subResultKey : resultSection.getKeys(false)) {
          final ConfigurationSection subResultSection = resultSection.getConfigurationSection(subResultKey);
          if (subResultSection == null) continue;

          try {
            final TriggerAction action = ActionType.of(subResultKey);
            if (action == null) continue;

            this.triggerActions.add(action.init(subResultKey, subResultSection, EventType.of(triggerEventKey)));
          } catch (final Exception exception) {
            exception.printStackTrace();
          }
        }
      }
    }
  }

  public void run(final ModifiedEntity entity, final EventType type, final Event event) {
    this.triggerActions.forEach(triggerAction -> {
      if (!triggerAction.eventType().equals(type)) return;
      if (entity.entity() == null) {
        return;
      }

      triggerAction.run(entity, event);
    });
  }

}
