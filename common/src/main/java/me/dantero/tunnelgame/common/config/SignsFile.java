package me.dantero.tunnelgame.common.config;

import com.gmail.furkanaxx34.dlibrary.bukkit.transformer.resolvers.BukkitSnakeyaml;
import com.gmail.furkanaxx34.dlibrary.transformer.TransformedObject;
import com.gmail.furkanaxx34.dlibrary.transformer.TransformerPool;
import com.gmail.furkanaxx34.dlibrary.transformer.annotations.Exclude;
import com.gmail.furkanaxx34.dlibrary.transformer.annotations.Names;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import me.dantero.tunnelgame.common.Constants;
import me.dantero.tunnelgame.common.misc.SessionAddress;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;

/**
 * @author Furkan Doğan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Names(modifier = Names.Modifier.TO_LOWER_CASE, strategy = Names.Strategy.HYPHEN_CASE)
public class SignsFile extends TransformedObject {

  @Exclude
  public static final Map<SessionAddress, Location> signLocations = new ConcurrentHashMap<>();

  @Exclude
  private static File configPath;

  @Exclude
  private static TransformedObject instance;
  
  @Exclude
  private static YamlConfiguration configuration;

  public static void loadFile(final Plugin plugin) {
    if (SignsFile.instance == null) {
      configPath = new File(plugin.getDataFolder(), "signs.yml");
      SignsFile.instance = TransformerPool.create(new SignsFile())
        .withFile(configPath)
        .withResolver(new BukkitSnakeyaml());
      configuration = YamlConfiguration.loadConfiguration(configPath);
    }

    SignsFile.instance.initiate();
    read();
  }

  public static Optional<Map.Entry<SessionAddress, Location>> findFromLocation(Location location) {
    return SignsFile.signLocations.entrySet()
      .stream()
      .filter(entry -> entry.getValue().equals(location))
      .findFirst();
  }

  @SuppressWarnings("ConstantConditions")
  private static void read() {
    Objects.requireNonNull(configuration, "initiate first!");
    Objects.requireNonNull(configPath, "initiate first!");

    for (String key : configuration.getKeys(false)) {
      Matcher matcher = Constants.SESSION_ADDRESS_PATTERN.matcher(key);
      if (!matcher.matches()) continue;
      String server = matcher.group("server");
      String id = matcher.group("id");

      try {
        ConfigurationSection section = configuration.getConfigurationSection(key);
        if (section == null) continue;

        String world = section.getString("world");
        double x = section.getDouble("x");
        double y = section.getDouble("y");
        double z = section.getDouble("z");
        float yaw = Float.parseFloat(section.getString("yaw"));
        float pitch = Float.parseFloat(section.getString("pitch"));

        int sessionId = Integer.parseInt(id);

        signLocations.put(new SessionAddress(server, sessionId), new Location(Bukkit.getWorld(world), x, y, z, yaw, pitch));
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
  
  public static void save(String server, int id, Location location) {
    Objects.requireNonNull(configuration, "initiate first!");
    Objects.requireNonNull(configPath, "initiate first!");

    String key = String.format("%s-%s", server, id);
    SessionAddress sessionAddress = new SessionAddress(server, id);
    if (location == null) {
      signLocations.remove(sessionAddress);
      configuration.set(key, null);
    } else {
      signLocations.put(sessionAddress, location);

      World world = location.getWorld();
      double x = location.getX();
      double y = location.getY();
      double z = location.getZ();
      float yaw = location.getYaw();
      float pitch = location.getPitch();

      configuration.set(key + ".world", world.getName());
      configuration.set(key + ".x", x);
      configuration.set(key + ".y", y);
      configuration.set(key + ".z", z);
      configuration.set(key + ".yaw", yaw);
      configuration.set(key + ".pitch", pitch);
    }

    try {
      configuration.save(configPath);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
