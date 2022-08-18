package me.dantero.tunnelgame.common.config;

import com.gmail.furkanaxx34.dlibrary.bukkit.transformer.resolvers.BukkitSnakeyaml;
import com.gmail.furkanaxx34.dlibrary.transformer.TransformedObject;
import com.gmail.furkanaxx34.dlibrary.transformer.TransformerPool;
import com.gmail.furkanaxx34.dlibrary.transformer.annotations.Exclude;
import com.gmail.furkanaxx34.dlibrary.transformer.annotations.Names;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import me.dantero.tunnelgame.common.Constants;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.Objects;

/**
 * @author Furkan Doğan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Names(modifier = Names.Modifier.TO_LOWER_CASE, strategy = Names.Strategy.HYPHEN_CASE)
public class ConfigFile extends TransformedObject {

  public static int maxPlayers = 10;

  public static int startCountdown = 20;

  @Exclude
  private static TransformedObject instance;

  @Exclude
  private static YamlConfiguration configuration;

  public static void loadFile(final Plugin plugin) {
    if (ConfigFile.instance == null) {
      File configPath = new File(plugin.getDataFolder(), "config.yml");
      ConfigFile.instance = TransformerPool.create(new ConfigFile())
        .withFile(configPath)
        .withResolver(new BukkitSnakeyaml());
      configuration = YamlConfiguration.loadConfiguration(configPath);
    }

    ConfigFile.instance.initiate();
  }

  public static Location getSpawnPoint() {
    Objects.requireNonNull(configuration, "initiate first!");
    return configuration.getLocation(Constants.SPAWN_POINT_KEY);
  }

  public static void setSpawnPoint(Location location) {
    Objects.requireNonNull(configuration, "initiate first!");
    configuration.set(Constants.SPAWN_POINT_KEY, location);
  }

  public static YamlConfiguration getConfiguration() {
    Objects.requireNonNull(configuration, "initiate first!");
    return configuration;
  }
}