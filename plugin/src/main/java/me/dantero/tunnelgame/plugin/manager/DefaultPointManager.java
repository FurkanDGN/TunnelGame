package me.dantero.tunnelgame.plugin.manager;

import me.dantero.tunnelgame.common.manager.PointManager;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Furkan Doğan
 */
public class DefaultPointManager implements PointManager {

  private static final Map<UUID, Integer> POINT_MAP = new HashMap<>();

  @Override
  public void addPoints(UUID uuid, int points) {
    int current = this.getPoints(uuid);
    POINT_MAP.put(uuid, current + points);
  }

  @Override
  public void removePoints(UUID uuid, int points) {
    int current = this.getPoints(uuid);
    POINT_MAP.put(uuid, Math.max(0, current - points));
  }

  @Override
  public boolean hasPoints(UUID uuid, int points) {
    int current = this.getPoints(uuid);
    return current >= points;
  }

  public int getPoints(UUID uuid) {
    return POINT_MAP.getOrDefault(uuid, 0);
  }

  @Override
  public void clearPoints(Player player) {
    UUID uniqueId = player.getUniqueId();
    POINT_MAP.remove(uniqueId);
  }
}
