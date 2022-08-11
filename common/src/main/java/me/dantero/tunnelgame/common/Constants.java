package me.dantero.tunnelgame.common;

import me.dantero.tunnelgame.common.util.time.TimeAPI;
import org.bukkit.NamespacedKey;

/**
 * @author Furkan Doğan
 */
@SuppressWarnings("SpellCheckingInspection")
public interface Constants {

  String SPAWN_POINT = "spawnpoint";

  int SPAWN_RANGE = 4;

  int DEFAULT_COOLDOWN = 5000;

  TimeAPI TIME_API = new TimeAPI();

  NamespacedKey OWNER_KEY = new NamespacedKey("tunnelgame", "owner");
}
