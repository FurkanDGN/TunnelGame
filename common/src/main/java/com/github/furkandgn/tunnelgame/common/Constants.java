package com.github.furkandgn.tunnelgame.common;

import com.github.furkandgn.tunnelgame.common.util.time.TimeAPI;
import org.bukkit.NamespacedKey;

import java.util.regex.Pattern;

/**
 * @author Furkan Doğan
 */
@SuppressWarnings("SpellCheckingInspection")
public interface Constants {

  String UPGRADES_KEY_FORMAT = "%s-%s";

  String SPAWN_POINT_KEY = "spawn-point";

  String ADMIN_PERMISSION = "tunnelgame.admin";

  String SIGN_FIRST_LINE = "tunnelgame";

  int SPAWN_RANGE = 4;

  int DEFAULT_COOLDOWN = 5000;

  TimeAPI TIME_API = new TimeAPI();

  Pattern LEVEL_PATTERN = Pattern.compile("^level-(?<level>\\d+)(-(?<range>\\d+))?");

  Pattern SESSION_ADDRESS_PATTERN = Pattern.compile("^(?<server>[A-z\\d-]+)-(?<id>\\d+)");

  NamespacedKey OWNER_KEY = new NamespacedKey("tunnelgame", "owner");

  NamespacedKey ROOT_KEY = new NamespacedKey("tunnelgame", "root");
}
