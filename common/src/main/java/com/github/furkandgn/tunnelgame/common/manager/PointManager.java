package com.github.furkandgn.tunnelgame.common.manager;

import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * @author Furkan Doğan
 */
public interface PointManager {

  void addPoints(UUID uuid, int points);

  default void addPoints(Player player, int points) {
    this.addPoints(player.getUniqueId(), points);
  }

  void removePoints(UUID uuid, int points);

  default void removePoints(Player player, int points) {
    this.removePoints(player.getUniqueId(), points);
  }

  boolean hasPoints(UUID uuid, int points);

  default boolean hasPoints(Player player, int points) {
    return this.hasPoints(player.getUniqueId(), points);
  }

  int getPoints(UUID uuid);

  default int getPoints(Player player) {
    return this.getPoints(player.getUniqueId());
  }

  void clearPoints(Player player);
}