package com.github.furkandgn.tunnelgame.common.game;

import com.github.furkandgn.tunnelgame.common.game.configuration.ModifiedEntitySetting;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * @author Furkan Doğan
 */
public interface SessionUtil {

  void startCountdown(int time, String messagePrefix, Consumer<Void> onEnd);

  Location currentLevelSpawnPoint();

  int calculateLevelLength(int level);

  void teleportPlayers(Location location);

  void sendTitle(String title, String subtitle);

  void sendActionBar(String message);

  void peekPlayers(Consumer<Player> playerConsumer);

  void teleportPlayersSpawn();

  Map<Integer, Level> buildLevels();

  void sendLobby(Player player);

  void kickPlayers();

  void startGameCountdown();

  void startGame();

  void prepareLevels(Map<Integer, Level> levels);

  World getWorld();

  void reloadWorld();

  void loadWorld();

  void spawnEntity(ModifiedEntitySetting modifiedEntitySetting);

  void clearEntities(Set<Integer> entityIds);
}
