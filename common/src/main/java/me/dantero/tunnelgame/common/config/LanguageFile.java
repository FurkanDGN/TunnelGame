package me.dantero.tunnelgame.common.config;

import com.gmail.furkanaxx34.dlibrary.bukkit.color.XColor;
import com.gmail.furkanaxx34.dlibrary.bukkit.transformer.resolvers.BukkitSnakeyaml;
import com.gmail.furkanaxx34.dlibrary.replaceable.RpString;
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

  public static RpString earnedPoints = RpString.from(
    "&aYou earned %points% points!"
  ).regex("%points%").map(XColor::colorize);

  public static RpString upgradeSuccess = RpString.from(
    "&aYou upgraded successfully!"
  ).map(XColor::colorize);

  public static RpString notEnoughPoints = RpString.from(
    "&eYou don't have enough points."
  ).map(XColor::colorize);

  public static RpString shouldOneByOne = RpString.from(
    "&eYou have to upgrade one by one."
  ).map(XColor::colorize);

  public static RpString gameStarting = RpString.from(
    "&6&lGame Starting"
  ).map(XColor::colorize);

  public static RpString gameStarted = RpString.from(
    "&A&lGame Started!"
  ).map(XColor::colorize);

  public static RpString waiting4Upgrades = RpString.from(
    "&e&lWaiting for upgrades"
  ).map(XColor::colorize);

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
