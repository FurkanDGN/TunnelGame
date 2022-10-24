package me.dantero.tunnelgame.common.config;

import com.gmail.furkanaxx34.dlibrary.bukkit.color.XColor;
import com.gmail.furkanaxx34.dlibrary.bukkit.transformer.resolvers.BukkitSnakeyaml;
import com.gmail.furkanaxx34.dlibrary.replaceable.RpList;
import com.gmail.furkanaxx34.dlibrary.replaceable.RpString;
import com.gmail.furkanaxx34.dlibrary.transformer.TransformedObject;
import com.gmail.furkanaxx34.dlibrary.transformer.TransformerPool;
import com.gmail.furkanaxx34.dlibrary.transformer.annotations.Comment;
import com.gmail.furkanaxx34.dlibrary.transformer.annotations.Exclude;
import com.gmail.furkanaxx34.dlibrary.transformer.annotations.Names;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import me.dantero.tunnelgame.common.Constants;
import me.dantero.tunnelgame.common.game.configuration.component.EquipmentComponent;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

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

  @Comment("If this server is lobby, set as true.")
  public static boolean lobbyMode = false;

  @Comment({"Bungeecord server name suffix",
    "#",
    "Name of server in Bungeecord should be like 'server mode-id' for example:",
    "If lobby: lobby-1",
    "If not: server-1"})
  public static String serverId = "1";

  @Comment("Maximum number of players that can enter a game.")
  public static int maxPlayers = 10;

  @Comment("Maximum number of game.")
  public static int maxSessionCount = 10;

  @Comment("Time to start the game.")
  public static int startCountdown = 20;

  @Comment("Time to make upgrades.")
  public static int upgradeCountdown = 15;

  @Comment("The height of the ground on the map.")
  public static int lowestBlockHeight = 70;

  @Comment("Points per monster kill.")
  public static int killRewardPoints = 4;

  @Comment("Which direction the tunnel is going.")
  public static BlockFace gameDirection = BlockFace.WEST;

  @Comment("Scoreboard title.")
  public static RpString scoreboardTitle = RpString.from("&6&lTunnel Game")
    .map(XColor::colorize);

  @Comment("Scoreboard lines.")
  public static RpList scoreboardLines = RpList.from(
      "&ePlayer: %player%",
      "&a",
      "&ePoints: %points%",
      "&a",
      "&eCurrent Level: %level%"
    )
    .map(XColor::colorize);

  @Comment("Lobby sign format.")
  public static RpList lobbySign = RpList.from(
      "&aTunnel Game",
      "&e%cur-pl%&8/&e%max-pl%",
      "%state%",
      "&7%server-suffix%-%session-id%"
    ).regex("%cur-pl%", "%max-pl%", "%state%", "%session-id%", "%server-suffix%")
    .map(XColor::colorize);

  @Comment("Redis configuration.")
  public static RedisConfig redisConfig = new RedisConfig();

  @Exclude
  public static EquipmentComponent starterEquipment = null;

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

    public static String host = "localhost";

    public static int port = 6379;

    public static String username = "admin";

    public static String password = "";
  }
}