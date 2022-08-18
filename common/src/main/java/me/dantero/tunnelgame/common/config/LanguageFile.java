package me.dantero.tunnelgame.common.config;

import com.gmail.furkanaxx34.dlibrary.bukkit.transformer.resolvers.BukkitSnakeyaml;
import com.gmail.furkanaxx34.dlibrary.transformer.TransformedObject;
import com.gmail.furkanaxx34.dlibrary.transformer.TransformerPool;
import com.gmail.furkanaxx34.dlibrary.transformer.annotations.Exclude;
import com.gmail.furkanaxx34.dlibrary.transformer.annotations.Names;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.plugin.Plugin;

import java.io.File;

/**
 * @author Furkan Doğan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Names(modifier = Names.Modifier.TO_LOWER_CASE, strategy = Names.Strategy.HYPHEN_CASE)
public class LanguageFile extends TransformedObject {

  @Exclude
  private static TransformedObject instance;

  public static void loadFile(final Plugin plugin) {
    if (LanguageFile.instance == null) {
      File configPath = new File(plugin.getDataFolder(), "language.yml");
      LanguageFile.instance = TransformerPool.create(new LanguageFile())
        .withFile(configPath)
        .withResolver(new BukkitSnakeyaml());
    }

    LanguageFile.instance.initiate();
  }
}
