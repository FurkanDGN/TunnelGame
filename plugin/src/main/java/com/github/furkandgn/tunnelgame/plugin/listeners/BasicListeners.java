package com.github.furkandgn.tunnelgame.plugin.listeners;

import com.destroystokyo.paper.event.player.PlayerInitialSpawnEvent;
import com.gmail.furkanaxx34.dlibrary.bukkit.utils.TaskUtilities;
import com.github.furkandgn.tunnelgame.common.Constants;
import com.github.furkandgn.tunnelgame.common.config.ConfigFile;
import com.github.furkandgn.tunnelgame.common.config.LanguageFile;
import com.github.furkandgn.tunnelgame.common.game.Level;
import com.github.furkandgn.tunnelgame.common.game.Listener;
import com.github.furkandgn.tunnelgame.common.handlers.JoinHandler;
import com.github.furkandgn.tunnelgame.common.manager.PointManager;
import com.github.furkandgn.tunnelgame.common.manager.SessionManager;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.Plugin;

import java.util.Map;
import java.util.Objects;
import java.util.stream.StreamSupport;

/**
 * @author Furkan Doğan
 */
@SuppressWarnings("SameParameterValue")
public class BasicListeners extends Listener {

  private final PointManager pointManager;
  private final JoinHandler joinHandler;

  public BasicListeners(Plugin plugin,
                        SessionManager sessionManager,
                        PointManager pointManager,
                        JoinHandler joinHandler) {
    super(plugin, sessionManager);
    this.pointManager = pointManager;
    this.joinHandler = joinHandler;
  }

  @Override
  public void register() {
    this.registerInGameEvents();
    this.registerNormalEvents();
  }

  private void registerNormalEvents() {
    this.listenEvent(CreatureSpawnEvent.class,
      event -> !event.getSpawnReason().equals(CreatureSpawnEvent.SpawnReason.CUSTOM) &&
        this.sessionManager.isSessionWorld(Objects.requireNonNull(event.getLocation().getWorld(), "World is null").getName()),
      event -> event.setCancelled(true)
    );

    this.listenEvent(CreatureSpawnEvent.class,
      event -> event.getSpawnReason().equals(CreatureSpawnEvent.SpawnReason.CUSTOM) &&
        this.sessionManager.isSessionWorld(Objects.requireNonNull(event.getLocation().getWorld(), "World is null").getName()),
      event -> TaskUtilities.syncLater(1, bukkitRunnable -> this.sessionManager.getSession(Objects.requireNonNull(event.getLocation().getWorld(), "World is null").getName())
        .ifPresent(session -> session.handleEntitySpawn(event.getEntity())))
    );

    this.listenEvent(PlayerInitialSpawnEvent.class,
      event -> this.joinHandler.getSpawnLocation(event.getPlayer())
        .ifPresent(event::setSpawnLocation));

    this.listenEvent(PlayerJoinEvent.class,
      event -> this.joinHandler.handle(event.getPlayer()));
  }

  private void registerInGameEvents() {
    this.listenInGameEvent(BlockBreakEvent.class,
      event -> !event.getPlayer().hasPermission(Constants.ADMIN_PERMISSION),
      (event, session) -> event.setCancelled(true));

    this.listenInGameEvent(BlockPlaceEvent.class,
      event -> !event.getPlayer().hasPermission(Constants.ADMIN_PERMISSION),
      (event, session) -> event.setCancelled(true));

    this.listenInGameEvent(EntityDamageByEntityEvent.class,
      event -> event.getDamager() instanceof Player && event.getEntity() instanceof Player,
      (event, session) -> event.setCancelled(true));

    this.listenInGameEvent(EntityDamageByEntityEvent.class,
      event -> event.getDamager() instanceof Player && event.getEntity() instanceof Monster,
      (event, session) -> {
        Location location = event.getEntity().getLocation();
        Level level = session.currentLevel();

        if (level.isPassed(location) || level.isOutBackside(location)) {
          event.setCancelled(true);
        }
      });

    this.listenInGameEvent(EntityDeathEvent.class,
      event -> event.getEntity() instanceof Monster,
      (event, session) -> {
        LivingEntity entity = event.getEntity();
        session.handleEntityDeath(entity);
        Player killer = entity.getKiller();
        if (killer == null) return;
        this.pointManager.addPoints(killer, ConfigFile.killRewardPoints);
        String message = LanguageFile.earnedPoints.build(Map.entry("%points%", () -> String.valueOf(ConfigFile.killRewardPoints)));
        killer.sendMessage(message);
      });

    this.listenInGameEvent(EntityExplodeEvent.class, (event, session) -> event.blockList().clear());

    this.listenInGameEvent(PlayerDeathEvent.class, (event, session) -> session.handlePlayerDeath(event.getEntity()));

    this.listenInGameEvent(PlayerRespawnEvent.class, (event, session) -> session.handlePlayerRespawn(event));

    this.listenInGameEvent(PlayerQuitEvent.class, (event, session) -> {
      session.handlePlayerQuit(event.getPlayer());
      this.pointManager.clearPoints(event.getPlayer());
    });

    this.listenInGameEvent(EntityDamageByEntityEvent.class, event -> event.getEntity() instanceof Player,
      (event, session) -> {
        Player player = (Player) event.getEntity();
        double health = player.getHealth();
        double finalDamage = event.getFinalDamage();
        if (health - finalDamage < 0.1d) {
          event.setCancelled(true);
          StreamSupport.stream(player.getInventory().spliterator(), false)
            .filter(Objects::nonNull)
            .forEach(itemStack -> player.getWorld().dropItem(player.getLocation(), itemStack));
          session.handlePlayerDeath(player);
        }
      });

    this.listenInGameEvent(EntityDamageByBlockEvent.class, event -> event.getEntity() instanceof Player,
      (event, session) -> {
        Player player = (Player) event.getEntity();
        double health = player.getHealth();
        double finalDamage = event.getFinalDamage();
        if (health - finalDamage <= 0) {
          event.setCancelled(true);
          StreamSupport.stream(player.getInventory().spliterator(), false)
            .filter(Objects::nonNull)
            .forEach(itemStack -> player.getWorld().dropItem(player.getLocation(), itemStack));
          session.handlePlayerDeath(player);
        }
      });
  }
}
