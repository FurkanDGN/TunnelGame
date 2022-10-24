package me.dantero.tunnelgame.common.manager;

import org.bukkit.Location;
import org.bukkit.block.Sign;

/**
 * @author Furkan Doğan
 */
public interface SignManager {

  void init();

  void save(Sign sign, String line2);

  void delete(Location location);
}
