package me.dantero.tunnelgame.plugin.listeners;

import me.dantero.tunnelgame.common.game.Listener;
import me.dantero.tunnelgame.common.game.configuration.component.entity.action.EventType;
import me.dantero.tunnelgame.common.manager.SessionManager;
import me.dantero.tunnelgame.plugin.TunnelGame;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

/**
 * @author Furkan Doğan
 */
public class ModifiedEntityListeners extends Listener {

  private static final NamespacedKey PROJECTILE_KEY = new NamespacedKey(JavaPlugin.getPlugin(TunnelGame.class), "projectile-source");

  public ModifiedEntityListeners(Plugin plugin,
                                 SessionManager sessionManager) {
    super(plugin, sessionManager);
  }

  @Override
  public void register() {
    this.listenInGameEvent(EntityDamageByEntityEvent.class,
      event -> event.getEntity() instanceof Player && event.getDamager() instanceof Monster,
      (event, session) -> session.retrieve(event.getDamager())
        .ifPresent(modifiedEntity -> modifiedEntity.entityAttribute().triggerAttribute()
          .ifPresent(triggerAttribute -> triggerAttribute.run(modifiedEntity, EventType.ON_DAMAGE, event))));

    this.listenInGameEvent(EntityDamageByEntityEvent.class,
      event -> event.getEntity() instanceof Monster && event.getDamager() instanceof Player,
      (event, session) -> session.retrieve(event.getEntity())
        .ifPresent(modifiedEntity -> modifiedEntity.entityAttribute().triggerAttribute()
          .ifPresent(triggerAttribute -> triggerAttribute.run(modifiedEntity, EventType.ON_ATTACK, event))));

    this.listenInGameEvent(PlayerMoveEvent.class,
      (event, session) -> session.getModifiedEntities()
        .forEach(modifiedEntity -> modifiedEntity.entityAttribute().triggerAttribute()
          .ifPresent(triggerAttribute -> triggerAttribute.run(modifiedEntity, EventType.PLAYER_DISTANCE, event))));

    this.listenInGameEvent(ProjectileLaunchEvent.class, (event, session) -> {
      Projectile projectile = event.getEntity();
      if (projectile.getShooter() instanceof Entity shooter) {
        final PersistentDataContainer container = projectile.getPersistentDataContainer();
        container.set(PROJECTILE_KEY, PersistentDataType.INTEGER, shooter.getEntityId());
      }
    });

    this.listenEvent(ProjectileHitEvent.class, event -> event.getHitEntity() instanceof Player, event -> {
      final Projectile projectile = event.getEntity();
      Player player = (Player) Objects.requireNonNull(event.getHitEntity());
      final PersistentDataContainer container = projectile.getPersistentDataContainer();
      final Integer shooterEntityId = container.get(PROJECTILE_KEY, PersistentDataType.INTEGER);
      if (shooterEntityId == null) {
        return;
      }

      this.sessionManager.getSession(player)
        .flatMap(session -> session.retrieve(shooterEntityId))
        .ifPresent(modifiedEntity -> player.addPotionEffect(modifiedEntity.entityAttribute().ammoAttribute().getApplicableEffect()));
    });
  }
}
