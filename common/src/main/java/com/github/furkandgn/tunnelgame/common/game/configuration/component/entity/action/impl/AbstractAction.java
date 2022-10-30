package com.github.furkandgn.tunnelgame.common.game.configuration.component.entity.action.impl;

import com.github.furkandgn.tunnelgame.common.game.configuration.ModifiedEntity;
import com.github.furkandgn.tunnelgame.common.game.configuration.component.entity.action.EventType;
import com.github.furkandgn.tunnelgame.common.game.configuration.component.entity.action.TriggerAction;
import com.github.furkandgn.tunnelgame.common.util.game.ControlUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * @author Furkan Doğan
 */
public abstract class AbstractAction implements TriggerAction {

  private long lastExecuted;
  private ConfigurationSection config;
  private EventType eventType;

  @Override
  public final EventType eventType() {
    return this.eventType;
  }

  @Override
  public final TriggerAction init(String actionType, ConfigurationSection section, EventType eventType) {
    this.eventType = eventType;
    this.config = section;
    this.init(this.config);
    return this;
  }

  @Override
  public final void run(ModifiedEntity entity, Event eventObject) {
    if (!this.canExecute()) {
      return;
    }

    switch (this.eventType) {
      case PLAYER_DISTANCE -> {
        PlayerMoveEvent event = (PlayerMoveEvent) eventObject;
        Player player = event.getPlayer();
        int activationRange = this.config.getInt("activation-range");

        if (!ControlUtils.isClose(entity, player, activationRange)) {
          this.onPlayerMove(entity, event);
          this.lastExecuted = System.currentTimeMillis();
        }
      }
      case ON_ATTACK -> {
        EntityDamageByEntityEvent event = (EntityDamageByEntityEvent) eventObject;
        this.onPlayerAttackEntity(entity, event);
        this.lastExecuted = System.currentTimeMillis();
      }
      case ON_DAMAGE -> {
        EntityDamageByEntityEvent event = (EntityDamageByEntityEvent) eventObject;
        this.onEntityAttackPlayer(entity, event);
        this.lastExecuted = System.currentTimeMillis();
      }
    }
  }

  protected abstract void init(ConfigurationSection configurationSection);

  protected abstract long getPeriod();

  protected void onPlayerMove(ModifiedEntity modifiedEntity, PlayerMoveEvent playerMoveEvent) {
  }

  protected void onPlayerAttackEntity(ModifiedEntity modifiedEntity, EntityDamageByEntityEvent entityDamageByEntityEvent) {
  }

  protected void onEntityAttackPlayer(ModifiedEntity modifiedEntity, EntityDamageByEntityEvent entityDamageByEntityEvent) {
  }

  private boolean canExecute() {
    return this.lastExecuted + this.getPeriod() <= System.currentTimeMillis();
  }
}
