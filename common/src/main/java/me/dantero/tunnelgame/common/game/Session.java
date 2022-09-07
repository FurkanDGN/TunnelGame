package me.dantero.tunnelgame.common.game;

import me.dantero.tunnelgame.common.game.configuration.ModifiedEntity;
import me.dantero.tunnelgame.common.game.state.JoinResultState;
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

  void handleLevelPass(Location location);

  void handleEntitySpawn(LivingEntity entity);

  void handleEntityDeath(LivingEntity livingEntity);

  void handlePlayerDeath(Player player);

  void handlePlayerRespawn(PlayerRespawnEvent event);

  Level currentLevel();

  JoinResultState tryJoinPlayer(Player player);

  boolean isInGame(Entity entity);

  Optional<ModifiedEntity> retrieve(Entity entity);

  Optional<ModifiedEntity> retrieve(int id);

  List<ModifiedEntity> getModifiedEntities();
}