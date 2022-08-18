package me.dantero.tunnelgame.plugin.listeners;

import com.gmail.furkanaxx34.dlibrary.bukkit.listeners.ListenerBasic;
import me.dantero.tunnelgame.common.Constants;
import me.dantero.tunnelgame.common.game.Listener;
import me.dantero.tunnelgame.common.handlers.JoinHandler;
import me.dantero.tunnelgame.common.manager.PointManager;
import me.dantero.tunnelgame.common.manager.SessionManager;
import me.dantero.tunnelgame.common.util.LocationUtil;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.Plugin;

import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * @author Furkan Doğan
 */
@SuppressWarnings("SameParameterValue")
public record BasicListeners(Plugin plugin,
                             SessionManager sessionManager,
                             PointManager pointManager,
                             JoinHandler joinHandler) implements Listener {

  @SuppressWarnings("ConstantConditions")
  @Override
  public void register() {
    this.listenInGameEvent(BlockBreakEvent.class,
      event -> !event.getPlayer().hasPermission(Constants.ADMIN_PERMISSION),
      event -> event.setCancelled(true));

    this.listenInGameEvent(BlockPlaceEvent.class,
      event -> !event.getPlayer().hasPermission(Constants.ADMIN_PERMISSION),
      event -> event.setCancelled(true));

    this.listenInGameEvent(EntityDamageByEntityEvent.class,
      event -> event.getDamager() instanceof Player damager && event.getEntity() instanceof Player entity &&
        (this.sessionManager.isInGame(damager) || this.sessionManager.isInGame(entity)),
      event -> event.setCancelled(true));

    this.listenEvent(EntityDeathEvent.class,
      event -> event.getEntity() instanceof Monster && event.getEntity().getKiller() != null,
      event -> this.pointManager.addPoints(event.getEntity().getKiller(), Constants.KILL_REWARD_POINTS));

    this.listenEvent(PlayerJoinEvent.class, event -> true, event -> this.joinHandler.handle(event.getPlayer()));
  }

  private <T extends Event> void listenEvent(Class<T> tClass, Predicate<T> predicate, Consumer<T> consumer) {
    new ListenerBasic<>(tClass, predicate, consumer).register(this.plugin);
  }

  private <T extends Event> void listenInGameEvent(Class<T> tClass, Consumer<T> consumer) {
    this.listenInGameEvent(tClass, t -> true, consumer);
  }

  private <T extends Event> void listenInGameEvent(Class<T> tClass, Predicate<T> predicate, Consumer<T> consumer) {
    Predicate<T> inGameCheck = unknownEvent -> {
      if (unknownEvent instanceof PlayerEvent event) {
        return this.sessionManager.isInGame(event.getPlayer());
      }
      if (unknownEvent instanceof BlockPlaceEvent event) {
        return this.sessionManager.isInGame(event.getPlayer());
      }
      if (unknownEvent instanceof BlockBreakEvent event) {
        return this.sessionManager.isInGame(event.getPlayer());
      }
      return false;
    };
    new ListenerBasic<>(tClass, inGameCheck.and(predicate), consumer).register(this.plugin);
  }
}
