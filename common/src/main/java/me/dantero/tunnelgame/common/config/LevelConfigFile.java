package me.dantero.tunnelgame.common.config;

import com.gmail.furkanaxx34.dlibrary.bukkit.transformer.resolvers.BukkitSnakeyaml;
import com.gmail.furkanaxx34.dlibrary.transformer.TransformedObject;
import com.gmail.furkanaxx34.dlibrary.transformer.TransformerPool;
import com.gmail.furkanaxx34.dlibrary.transformer.annotations.Exclude;
import com.gmail.furkanaxx34.dlibrary.transformer.annotations.Names;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import me.dantero.tunnelgame.common.Constants;
import me.dantero.tunnelgame.common.game.configuration.LevelConfiguration;
import me.dantero.tunnelgame.common.game.configuration.ModifiedEntitySetting;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;

/**
 * @author Furkan Doğan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Names(modifier = Names.Modifier.TO_LOWER_CASE, strategy = Names.Strategy.HYPHEN_CASE)
public class LevelConfigFile extends TransformedObject {

  @Exclude
  public static LevelConfiguration levelConfiguration;

  @Exclude
  @Nullable
  private static TransformedObject instance;

  @Exclude
  @Nullable
  private static YamlConfiguration configuration;

  public static void loadFile(final Plugin plugin) {
    if (LevelConfigFile.instance == null) {
      File configPath = new File(plugin.getDataFolder(), "level-config.yml");
      LevelConfigFile.instance = TransformerPool.create(new LevelConfigFile())
        .withFile(configPath)
        .withResolver(new BukkitSnakeyaml());
      configuration = YamlConfiguration.loadConfiguration(configPath);
    }

    LevelConfigFile.instance.initiate();
    build();
  }

  private static void build() {
    Objects.requireNonNull(configuration, "initiate first");

    Map<Integer, Integer> levelLength = new HashMap<>();
    Map<Integer, List<ModifiedEntitySetting>> levelEntities = new HashMap<>();

    Set<String> keys = configuration.getKeys(false);
    for (String key : keys) {
      Matcher matcher = Constants.LEVEL_PATTERN.matcher(key);
      if (!matcher.matches()) continue;
      int level = Integer.parseInt(matcher.group("level"));
      String range = matcher.group("range");
      ConfigurationSection configurationSection = configuration.getConfigurationSection(key);
      if (configurationSection == null) continue;
      int length = configurationSection.getInt("goal-distance");
      ConfigurationSection entities = configurationSection.getConfigurationSection("entities");
      if (entities == null) continue;
      List<ModifiedEntitySetting> modifiedEntitySettings = entities.getKeys(false).stream()
        .map(entityName -> {
          ConfigurationSection entityConfig = entities.getConfigurationSection(entityName);
          if (entityConfig == null) return null;

          return new ModifiedEntitySetting(entityName, entityConfig);
        })
        .filter(Objects::nonNull)
        .toList();

      levelLength.put(level, length);
      levelEntities.put(level, modifiedEntitySettings);

      if (range != null) {
        for (int i = level + 1; i <= Integer.parseInt(range); i++) {
          levelLength.put(i, length);
          levelEntities.put(i, modifiedEntitySettings);
        }
      }
    }

    levelConfiguration = new LevelConfiguration(levelLength, levelEntities);
  }

}
