package me.dantero.tunnelgame.common.config;

import com.gmail.furkanaxx34.dlibrary.bukkit.transformer.resolvers.BukkitSnakeyaml;
import com.gmail.furkanaxx34.dlibrary.transformer.TransformedObject;
import com.gmail.furkanaxx34.dlibrary.transformer.TransformerPool;
import com.gmail.furkanaxx34.dlibrary.transformer.annotations.Exclude;
import com.gmail.furkanaxx34.dlibrary.transformer.annotations.Names;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import me.dantero.tunnelgame.common.config.pojo.UpgradeConfig;
import me.dantero.tunnelgame.common.game.configuration.component.EquipmentComponent;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Furkan Doğan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Names(modifier = Names.Modifier.TO_LOWER_CASE, strategy = Names.Strategy.HYPHEN_CASE)
public class UpgradeConfigFile extends TransformedObject {

  @Exclude
  private static final Pattern levelPattern = Pattern.compile("^level-(?<level>\\d+)");

  @Exclude
  public static Map<Integer, UpgradeConfig> selfArmor = new HashMap<>();

  @Exclude
  public static Map<Integer, UpgradeConfig> selfFood = new HashMap<>();

  @Exclude
  public static Map<Integer, UpgradeConfig> selfWeapon = new HashMap<>();

  @Exclude
  public static Map<Integer, UpgradeConfig> teamSharpness = new HashMap<>();

  @Exclude
  public static Map<Integer, UpgradeConfig> teamProtection = new HashMap<>();

  @Exclude
  @Nullable
  private static TransformedObject instance;

  @Exclude
  @Nullable
  private static YamlConfiguration configuration;

  public static void loadFile(final Plugin plugin) {
    if (UpgradeConfigFile.instance == null) {
      File configPath = new File(plugin.getDataFolder(), "upgrade-config.yml");
      UpgradeConfigFile.instance = TransformerPool.create(new UpgradeConfigFile())
        .withFile(configPath)
        .withResolver(new BukkitSnakeyaml());
      configuration = YamlConfiguration.loadConfiguration(configPath);
    }

    selfArmor.putAll(buildEquipmentUpgrades(UpgradePath.SELF_ARMOR));
    selfFood.putAll(buildEquipmentUpgrades(UpgradePath.SELF_FOOD));
    selfWeapon.putAll(buildEquipmentUpgrades(UpgradePath.SELF_WEAPON));
    teamSharpness.putAll(buildIntegerUpgrades(UpgradePath.TEAM_SHARPNESS));
    teamProtection.putAll(buildIntegerUpgrades(UpgradePath.TEAM_PROTECTION));

    UpgradeConfigFile.instance.initiate();
  }

  private static Map<Integer, UpgradeConfig> buildEquipmentUpgrades(String path) {
    Objects.requireNonNull(configuration, "initiate first");
    Map<Integer, UpgradeConfig> map = new HashMap<>();
    ConfigurationSection section = configuration.getConfigurationSection(path);
    Objects.requireNonNull(section, "Section " + path + " is null");

    for (String key : section.getKeys(false)) {
      Matcher matcher = levelPattern.matcher(key);
      if (!matcher.matches()) continue;

      int level = Integer.parseInt(matcher.group("level"));
      String equipmentFormat = String.format("%s.equipment", key);
      String requiredPointsFormat = String.format("%s.points", key);

      int points = section.getInt(requiredPointsFormat, 0);
      ConfigurationSection configurationSection = section.getConfigurationSection(equipmentFormat);
      EquipmentComponent equipmentComponent = new EquipmentComponent(configurationSection);

      UpgradeConfig upgradeConfig = new UpgradeConfig(points, equipmentComponent);
      map.put(level, upgradeConfig);
    }

    return map;
  }

  private static Map<Integer, UpgradeConfig> buildIntegerUpgrades(String path) {
    Objects.requireNonNull(configuration, "initiate first");
    Map<Integer, UpgradeConfig> map = new HashMap<>();
    ConfigurationSection section = configuration.getConfigurationSection(path);
    Objects.requireNonNull(section, "Section " + path + " is null");

    for (String key : section.getKeys(false)) {
      Matcher matcher = levelPattern.matcher(key);
      if (!matcher.matches()) continue;

      String requiredPointsFormat = String.format("%s.points", key);
      String enchantLevelFormat = String.format("%s.enchant-level", key);
      int level = Integer.parseInt(matcher.group("level"));

      int enchantmentLevel = section.getInt(enchantLevelFormat, 1);
      int points = section.getInt(requiredPointsFormat, 1);

      UpgradeConfig upgradeConfig = new UpgradeConfig(points, enchantmentLevel);
      map.put(level, upgradeConfig);
    }

    return map;
  }
}
