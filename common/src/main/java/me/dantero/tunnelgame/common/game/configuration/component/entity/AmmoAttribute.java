package me.dantero.tunnelgame.common.game.configuration.component.entity;

import me.dantero.tunnelgame.common.util.RandomUtil;
import me.dantero.tunnelgame.common.util.time.TimeAPI;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public final class AmmoAttribute {

  private final Map<PotionEffect, Integer> effects = new HashMap<>();

  public AmmoAttribute(final @NotNull ConfigurationSection configuration) {
    final ConfigurationSection ammoAttributes = configuration.getConfigurationSection("ammo-attribute");
    if (ammoAttributes != null) {
      for (final String key : ammoAttributes.getKeys(false)) {
        final ConfigurationSection ammoAttribute = ammoAttributes.getConfigurationSection(key);

        if (ammoAttribute == null) continue;
        final PotionEffectType type = PotionEffectType.getByName(ammoAttribute.getString("type", "none"));
        final int level = ammoAttribute.getInt("level") + 1;
        final long duration = new TimeAPI().parseTime(ammoAttribute.getString("duration", "5sec")).to(TimeUnit.SECONDS);

        if (type == null) {
          throw new RuntimeException("Failed to create potion effect with the type '" + ammoAttribute.getString("type") + "'");
        }

        this.effects.put(new PotionEffect(type, (int) duration, level, false, false, false), ammoAttribute.getInt("chance"));
      }
    }
  }

  public PotionEffect getApplicableEffect() {
    int retryCount = 0;

    if (!this.effects.isEmpty()) {
      while (retryCount < 100) {
        for (final PotionEffect effect : this.effects.keySet()) {
          final int chance = this.effects.get(effect);

          if (RandomUtil.randomIntInRange(100) < chance) {
            return effect;
          }
        }

        retryCount++;
      }

      throw new RuntimeException("Something went wrong.");
    } else { // Empty potion effect
      return PotionEffectType.LUCK.createEffect(0, 1);
    }
  }

}
