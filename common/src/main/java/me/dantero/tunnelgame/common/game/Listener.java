package me.dantero.tunnelgame.common.game;

import com.gmail.furkanaxx34.dlibrary.bukkit.listeners.ListenerBasic;
import me.dantero.tunnelgame.common.manager.SessionManager;
import me.dantero.tunnelgame.common.util.SessionListener;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.projectiles.ProjectileSource;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author Furkan Doğan
 */
@SuppressWarnings("SameParameterValue")
public abstract class Listener {

  protected Plugin plugin;
  protected SessionManager sessionManager;

  protected Listener(Plugin plugin,
                     SessionManager sessionManager) {
    this.plugin = plugin;
    this.sessionManager = sessionManager;
  }

  public abstract void register();

  protected  <T extends Event> void listenEvent(Class<T> tClass, Predicate<T> predicate, Consumer<T> consumer) {
    new ListenerBasic<>(tClass, predicate, consumer).register(this.plugin);
  }

  protected  <T extends Event> void listenInGameEvent(Class<T> tClass, BiConsumer<T, Session> consumer) {
    this.listenInGameEvent(tClass, t -> true, consumer);
  }

  protected  <T extends Event> void listenInGameEvent(Class<T> tClass, Predicate<T> predicate, BiConsumer<T, Session> consumer) {
    Function<T, Optional<Session>> inGameCheck = unknownEvent -> {
      if (unknownEvent instanceof PlayerEvent event) {
        return this.sessionManager.getSession(event.getPlayer());
      }
      if (unknownEvent instanceof BlockPlaceEvent event) {
        return this.sessionManager.getSession(event.getPlayer());
      }
      if (unknownEvent instanceof BlockBreakEvent event) {
        return this.sessionManager.getSession(event.getPlayer());
      }
      if (unknownEvent instanceof EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        return this.sessionManager.getSession(entity);
      }
      if (unknownEvent instanceof CreatureSpawnEvent event) {
        LivingEntity entity = event.getEntity();
        return this.sessionManager.getSession(entity);
      }
      if (unknownEvent instanceof EntityDamageByEntityEvent event) {
        return this.findSession(event);
      }
      if (unknownEvent instanceof ProjectileLaunchEvent event) {
        Projectile entity = event.getEntity();
        ProjectileSource shooter = entity.getShooter();
        return this.sessionManager.getSession((Entity) shooter);
      }
      return Optional.empty();
    };
    new SessionListener<>(tClass, inGameCheck, predicate, consumer).register(this.plugin);
  }

  private Optional<Session> findSession(EntityDamageByEntityEvent event) {
    Entity damagerEntity = event.getDamager();
    Entity entity = event.getEntity();

    if (damagerEntity instanceof Player damager) {
      Optional<Session> damagerSession = this.sessionManager.getSession(damager);

      if (entity instanceof Player player) {
        Optional<Session> entitySession = this.sessionManager.getSession(player);

        if (entitySession.isPresent() && damagerSession.isPresent() && entitySession.get().equals(damagerSession.get())) {
          return entitySession;
        }
      } else {
        return damagerSession;
      }
    } else if (entity instanceof Player player) {
      return this.sessionManager.getSession(player);
    } else {
      Optional<Session> damagerSession = this.sessionManager.getSession(damagerEntity);
      Optional<Session> entitySession = this.sessionManager.getSession(entity);

      if (entitySession.isPresent() && damagerSession.isPresent() && entitySession.get().equals(damagerSession.get())) {
        return entitySession;
      }
    }

    return Optional.empty();
  }
}
