package me.dantero.tunnelgame.common.config;

import com.gmail.furkanaxx34.dlibrary.bukkit.transformer.resolvers.BukkitSnakeyaml;
import com.gmail.furkanaxx34.dlibrary.transformer.TransformedObject;
import com.gmail.furkanaxx34.dlibrary.transformer.TransformerPool;
import com.gmail.furkanaxx34.dlibrary.transformer.annotations.Exclude;
import com.gmail.furkanaxx34.dlibrary.transformer.annotations.Names;
import org.bukkit.plugin.Plugin;

import java.io.File;

/**
 * @author Furkan Doğan
 */
@Names(modifier = Names.Modifier.TO_LOWER_CASE, strategy = Names.Strategy.HYPHEN_CASE)
public class ConfigFile extends TransformedObject {



  @Exclude
  private static TransformedObject instance;

  public static void loadFile(final Plugin plugin) {
    if (ConfigFile.instance == null) {
      ConfigFile.instance = TransformerPool.create(new ConfigFile())
        .withFile(new File(plugin.getDataFolder(), "config.yml"))
        .withResolver(new BukkitSnakeyaml());
    }

    ConfigFile.instance.initiate();
  }

}
