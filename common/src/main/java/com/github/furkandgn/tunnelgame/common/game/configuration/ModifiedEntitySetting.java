package com.github.furkandgn.tunnelgame.common.game.configuration;

import com.github.furkandgn.tunnelgame.common.game.configuration.component.EquipmentComponent;
import com.github.furkandgn.tunnelgame.common.game.configuration.component.entity.AmmoAttribute;
import com.github.furkandgn.tunnelgame.common.game.configuration.component.entity.TriggerAttribute;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ModifiedEntitySetting {

  private final EquipmentComponent equipmentComponent;
  private final TriggerAttribute triggerAttribute;
  private final Map<Attribute, Double> entityAttributes = new HashMap<>();
  private final AmmoAttribute ammoAttribute;
  private final EntityType entityType;
  private final String entityDisplayName;
  private final boolean selfDamageResistance;
  private final String identifier;

  private final int count;

  public ModifiedEntitySetting(final String identifier, final @NotNull ConfigurationSection config) {
    this.identifier = identifier;
    this.ammoAttribute = new AmmoAttribute(config);
    this.entityType = EntityType.valueOf(config.getString("type"));
    this.selfDamageResistance = config.getString("damage-resistance", "false").equalsIgnoreCase("SELF");
    this.entityDisplayName = config.getString("display-name");
    this.equipmentComponent = new EquipmentComponent(config.getConfigurationSection("equipment"));
    this.count = config.getInt("count", 1);

    final List<String> attributes = config.getStringList("attribute");
    if (!attributes.isEmpty()) {
      for (final String attributeRaw : attributes) {
        String[] split = attributeRaw.split(":");
        if (split.length != 2) continue;

        this.entityAttributes.put(
          Attribute.valueOf(split[0]),
          Double.parseDouble(split[1])
        );
      }
    }

    final ConfigurationSection triggers = config.getConfigurationSection("trigger");
    if (triggers == null) {
      this.triggerAttribute = null;
    } else {
      this.triggerAttribute = new TriggerAttribute(triggers);
    }
  }

  public ModifiedEntity create(@NotNull final String worldName) {
    return new ModifiedEntity(this, worldName);
  }

  public int getCount() {
    return this.count;
  }

  TriggerAttribute getTriggerAttribute() {
    return this.triggerAttribute;
  }

  Map<Attribute, Double> getEntityAttributes() {
    return this.entityAttributes;
  }

  AmmoAttribute getAmmoAttribute() {
    return this.ammoAttribute;
  }

  EntityType getEntityType() {
    return this.entityType;
  }

  String getEntityDisplayName() {
    return this.entityDisplayName;
  }

  boolean isSelfDamageResistance() {
    return this.selfDamageResistance;
  }

  public EquipmentComponent getEquipmentComponent() {
    return this.equipmentComponent;
  }

  public String getIdentifier() {
    return this.identifier;
  }
}
