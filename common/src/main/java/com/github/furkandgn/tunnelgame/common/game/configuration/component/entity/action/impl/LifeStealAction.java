package com.github.furkandgn.tunnelgame.common.game.configuration.component.entity.action.impl;

import com.github.furkandgn.tunnelgame.common.Constants;
import com.github.furkandgn.tunnelgame.common.game.configuration.ModifiedEntity;
import com.github.furkandgn.tunnelgame.common.util.game.RandomUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Damageable;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.Objects;

/**
 * @author Furkan Doğan
 */
public class LifeStealAction extends AbstractAction {

  private int stealFromRange;
  private int stealToRange;

  @Override
  protected void init(ConfigurationSection configurationSection) {
    String stealRangeRaw = Objects.requireNonNull(configurationSection.getString("steal-range"), "Steal range cannot be null");
    String[] parts = stealRangeRaw.split(":");
    this.stealFromRange = Integer.parseInt(parts[0]);
    this.stealToRange = Integer.parseInt(parts[1]);
  }

  @Override
  protected void onEntityAttackPlayer(ModifiedEntity modifiedEntity, EntityDamageByEntityEvent event) {
    Damageable damager = (Damageable) event.getDamager();
    double finalDamage = event.getFinalDamage();
    double health = damager.getHealth();
    double finalHealth = health - finalDamage;
    if (finalHealth <= 10) {
      int lifeSteal = RandomUtil.randomIntInRange(this.stealFromRange, this.stealToRange);
      damager.setHealth(finalHealth + lifeSteal);
    }
  }

  @Override
  protected long getPeriod() {
    return Constants.DEFAULT_COOLDOWN;
  }
}
