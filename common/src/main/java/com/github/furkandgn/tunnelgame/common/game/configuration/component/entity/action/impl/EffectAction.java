package com.github.furkandgn.tunnelgame.common.game.configuration.component.entity.action.impl;

import com.github.furkandgn.tunnelgame.common.Constants;
import com.github.furkandgn.tunnelgame.common.game.configuration.ModifiedEntity;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public final class EffectAction extends AbstractAction {

  private PotionEffectType effectType;
  private Integer level;
  private Long duration;
  private String target;

  @Override
  protected void init(ConfigurationSection config) {
    String effectType = Objects.requireNonNull(config.getString("effect-type"), "Effect type cannot be null");
    this.effectType = PotionEffectType.getByName(effectType);
    this.target = config.getString("target");
    this.level = config.getInt("level");
    String durationRaw = config.getString("duration");
    this.duration = Constants.TIME_API.parseTime(durationRaw).to(TimeUnit.SECONDS);
  }

  @Override
  public void onPlayerMove(ModifiedEntity modifiedEntity, PlayerMoveEvent event) {
    final PotionEffect potionEffect = new PotionEffect(this.effectType, this.duration.intValue() * 20, this.level);
    final LivingEntity livingEntity = (LivingEntity) modifiedEntity.entity();

    if (this.target.equalsIgnoreCase("SELF")) {
      livingEntity.addPotionEffect(potionEffect);
    } else {
      event.getPlayer().addPotionEffect(potionEffect);
    }
  }

  @Override
  protected void onEntityAttackPlayer(ModifiedEntity modifiedEntity, EntityDamageByEntityEvent event) {
    final PotionEffect potionEffect = new PotionEffect(this.effectType, this.duration.intValue() * 20, this.level);
    final LivingEntity livingEntity = (LivingEntity) modifiedEntity.entity();

    if (this.target.equalsIgnoreCase("SELF")) {
      livingEntity.addPotionEffect(potionEffect);
    } else {
      ((Player) event.getEntity()).addPotionEffect(potionEffect);
    }
  }

  @Override
  protected long getPeriod() {
    return Constants.DEFAULT_COOLDOWN;
  }
}
