package me.dantero.tunnelgame.plugin.listeners;

import me.dantero.tunnelgame.common.Constants;
import me.dantero.tunnelgame.common.config.ConfigFile;
import me.dantero.tunnelgame.common.config.LanguageFile;
import me.dantero.tunnelgame.common.game.Listener;
import me.dantero.tunnelgame.common.handlers.JoinHandler;
import me.dantero.tunnelgame.common.manager.PointManager;
import me.dantero.tunnelgame.common.manager.SessionManager;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.Plugin;

import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map;
import java.util.Objects;

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
      event -> this.sessionManager.getSession(Objects.requireNonNull(event.getLocation().getWorld(), "World is null").getName())
        .ifPresent(session -> session.handleEntitySpawn(event.getEntity()))
    );

    this.listenEvent(PlayerJoinEvent.class, event -> true, event -> this.joinHandler.handle(event.getPlayer()));
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

    this.listenInGameEvent(EntityDeathEvent.class,
      event -> event.getEntity() instanceof Monster,
      (event, session) -> {
        LivingEntity entity = event.getEntity();
        Player killer = entity.getKiller();
        if (killer == null) return;
        this.pointManager.addPoints(killer, ConfigFile.killRewardPoints);
        String message = LanguageFile.earnedPoints.build(new SimpleEntry<>("%points%", () -> String.valueOf(ConfigFile.killRewardPoints)));
        killer.sendMessage(message);
        session.handleEntityDeath(entity);
      });

    this.listenInGameEvent(EntityExplodeEvent.class, (event, session) -> event.blockList().clear());

    this.listenInGameEvent(PlayerDeathEvent.class, (event, session) -> session.handlePlayerDeath(event.getEntity()));

    this.listenInGameEvent(PlayerRespawnEvent.class, (event, session) -> session.handlePlayerRespawn(event));
  }
}
