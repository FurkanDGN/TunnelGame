package com.github.furkandgn.tunnelgame.common.game.configuration.component.entity.action;

import com.github.furkandgn.tunnelgame.common.game.configuration.ModifiedEntity;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.Event;

public interface TriggerAction {

  EventType eventType();

  TriggerAction init(final String key, final ConfigurationSection section, final EventType eventType);

  void run(final ModifiedEntity entity, final Event eventObject);

}