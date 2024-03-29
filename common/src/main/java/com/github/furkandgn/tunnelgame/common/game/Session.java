package com.github.furkandgn.tunnelgame.common.game;

import com.github.furkandgn.tunnelgame.common.game.configuration.LevelConfiguration;
import com.github.furkandgn.tunnelgame.common.game.configuration.ModifiedEntity;
import com.github.furkandgn.tunnelgame.common.game.state.JoinResultState;
import com.github.furkandgn.tunnelgame.common.manager.InventoryManager;
import com.github.furkandgn.tunnelgame.common.manager.MapManager;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.util.List;
import java.util.Optional;

/**
 * @author Furkan Doğan
 */
public interface Session {

  SessionContext getSessionContext();

  int sessionId();

  void prepare();

  void start();

  void togglePause();

  void stop();

  void shutdown();

  void restart();

  void handleLevelPass(Location location);

  boolean levelGoalsCompleted();

  void handleEntitySpawn(LivingEntity entity);

  void handleEntityDeath(LivingEntity livingEntity);

  void handlePlayerDeath(Player player);

  void handlePlayerRespawn(PlayerRespawnEvent event);

  void handlePlayerQuit(Player player);

  Level currentLevel();

  JoinResultState tryJoinPlayer(Player player);

  boolean isInGame(Entity entity);

  Optional<ModifiedEntity> retrieve(Entity entity);

  Optional<ModifiedEntity> retrieve(int id);

  List<ModifiedEntity> getModifiedEntities();

  LevelConfiguration getLevelConfiguration();

  MapManager getMapManager();

  InventoryManager getInventoryManager();
}