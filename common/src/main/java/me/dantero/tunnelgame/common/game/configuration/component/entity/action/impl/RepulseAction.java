package me.dantero.tunnelgame.common.game.configuration.component.entity.action.impl;

import me.dantero.tunnelgame.common.game.configuration.ModifiedEntity;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.util.Vector;

/**
 * @author Furkan Doğan
 */
public class RepulseAction extends AbstractAction {

  private int power;

  @Override
  protected void init(ConfigurationSection configurationSection) {
    this.power = configurationSection.getInt("power");
  }

  @Override
  protected void onEntityAttackPlayer(ModifiedEntity modifiedEntity, EntityDamageByEntityEvent event) {
    Entity entity = event.getEntity();
    Entity damager = event.getDamager();

    Vector vector = this.calculateVector(damager);
    Vector finalVector = vector.multiply(this.power * 2);

    entity.setVelocity(finalVector);
  }

  @Override
  protected long getPeriod() {
    return 2000L;
  }

  private Vector calculateVector(Entity entity) {
    Location location = entity.getLocation();
    float entityYaw = location.getYaw();
    float entityPitch = location.getPitch();

    double yaw = ((entityYaw + 90) * Math.PI) / 180;
    double pitch = ((entityPitch + 90) * Math.PI) / 180;
    double x = Math.sin(pitch) * Math.cos(yaw);
    double z = Math.cos(pitch);

    return new Vector(x, 0.5, z);
  }
}
