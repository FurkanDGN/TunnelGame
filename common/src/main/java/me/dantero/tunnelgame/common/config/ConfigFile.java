package me.dantero.tunnelgame.common.config;

import com.gmail.furkanaxx34.dlibrary.bukkit.color.XColor;
import com.gmail.furkanaxx34.dlibrary.bukkit.transformer.resolvers.BukkitSnakeyaml;
import com.gmail.furkanaxx34.dlibrary.replaceable.RpList;
import com.gmail.furkanaxx34.dlibrary.replaceable.RpString;
import com.gmail.furkanaxx34.dlibrary.transformer.TransformedObject;
import com.gmail.furkanaxx34.dlibrary.transformer.TransformerPool;
import com.gmail.furkanaxx34.dlibrary.transformer.annotations.Exclude;
import com.gmail.furkanaxx34.dlibrary.transformer.annotations.Names;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import me.dantero.tunnelgame.common.Constants;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Furkan Doğan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Names(modifier = Names.Modifier.TO_LOWER_CASE, strategy = Names.Strategy.HYPHEN_CASE)
public class ConfigFile extends TransformedObject {

  public static boolean lobbyMode = false;

  public static String serverId = "1";

  public static int maxPlayers = 10;

  public static int maxSessionCount = 10;

  public static int startCountdown = 20;

  public static int upgradeCountdown = 15;

  public static int lowestBlockHeight = 70;

  public static int killRewardPoints = 4;

  public static BlockFace gameDirection = BlockFace.WEST;

  public static RpString scoreboardTitle = RpString.from("&6&lTunnel Game")
    .map(XColor::colorize);

  public static RpList scoreboardLines = RpList.from(
    "&ePlayer: %player%",
    "&a",
    "&ePoints: %points%",
    "&a",
    "&eCurrent Level: %level%"
    )
    .map(XColor::colorize);

  public static RedisConfig redisConfig = new RedisConfig();

  @Exclude
  private static TransformedObject instance;

  public static void loadFile(final Plugin plugin) {
    File configPath = new File(plugin.getDataFolder(), "config.yml");
    if (ConfigFile.instance == null) {
      ConfigFile.instance = TransformerPool.create(new ConfigFile())
        .withFile(configPath)
        .withResolver(new BukkitSnakeyaml());
    }

    ConfigFile.instance.initiate();
  }

  public static Location getSpawnPoint() {
    Objects.requireNonNull(ConfigFile.instance, "initiate first!");
    Optional<Map> locationMap = ConfigFile.instance.get(Constants.SPAWN_POINT_KEY, Map.class);
    return locationMap.map(map -> {
      double x = Double.parseDouble(map.get("x").toString());
      double y = Double.parseDouble(map.get("y").toString());
      double z = Double.parseDouble(map.get("z").toString());
      float yaw = Float.parseFloat(map.get("yaw").toString());
      float pitch = Float.parseFloat(map.get("pitch").toString());
      return new Location(null, x, y, z, yaw, pitch);
    }).orElse(null);
  }

  public static final class RedisConfig extends TransformedObject {

    public static String redisHost = "localhost";

    public static String redisUsername = "admin";

    public static String redisPassword = "";
  }
}