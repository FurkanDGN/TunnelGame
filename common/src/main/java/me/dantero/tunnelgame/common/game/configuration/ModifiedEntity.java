package me.dantero.tunnelgame.common.game.configuration;

import me.dantero.tunnelgame.common.game.configuration.component.EquipmentComponent;
import me.dantero.tunnelgame.common.game.configuration.component.entity.AmmoAttribute;
import me.dantero.tunnelgame.common.game.configuration.component.entity.TriggerAttribute;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Objects;

public final class ModifiedEntity {

  private final EntityAttribute entityAttribute;
  private final EntityModifier entityModifier;
  private final Location eggLocation;
  private final String worldName;

  private int id;

  public ModifiedEntity(final ModifiedEntitySetting setting, final Location location) {
    this.entityAttribute = new EntityAttribute(
      setting.getTriggerAttribute(),
      setting.getAmmoAttribute(),
      setting.getEntityAttributes()
    );

    this.entityModifier = new EntityModifier(
      setting.getEntityType(),
      setting.getEntityDisplayName(),
      setting.isSelfDamageResistance(),
      setting.getEquipmentComponent()
    );
    this.eggLocation = location.clone().add(0, 2, 0);
    this.worldName = location.getWorld().getName();
  }

  public Entity initiate(@Nullable final Location location) {
    Location spawnLocation = location == null ? this.eggLocation : location;
    final Entity spawnedEntity = spawnLocation.getWorld().spawnEntity(spawnLocation, this.entityModifier.entityType, CreatureSpawnEvent.SpawnReason.CUSTOM);

    spawnedEntity.customName(LegacyComponentSerializer.legacyAmpersand().deserialize(this.entityModifier.displayName));
    this.entityAttribute.apply(spawnedEntity);
    this.entityModifier.equipmentComponent.apply(spawnedEntity);

    this.id = spawnedEntity.getEntityId();
    return spawnedEntity;
  }

  public int id() {
    return this.id;
  }

  public Entity entity() {
    return Objects.requireNonNull(Bukkit.getWorld(this.worldName), "World cannot found")
      .getLivingEntities()
      .stream()
      .filter(livingEntity -> livingEntity.getEntityId() == this.id)
      .findFirst()
      .orElse(null);
  }

  public boolean selfDamageable() {
    return this.entityModifier.selfDamageable;
  }

  public EntityModifier entityModifier() {
    return this.entityModifier;
  }

  public EntityAttribute entityAttribute() {
    return this.entityAttribute;
  }

  public static final class EntityModifier {

    private final EquipmentComponent equipmentComponent;
    private final boolean selfDamageable;
    private final EntityType entityType;
    private final String displayName;

    private Player owner;
    private boolean respawned;

    public EntityModifier(final EntityType type, final String name, final boolean selfDamageable, final EquipmentComponent equipmentComponent) {
      this.entityType = type;
      this.displayName = name;
      this.selfDamageable = selfDamageable;
      this.equipmentComponent = equipmentComponent;
    }

    public void owner(final Player player) {
      this.owner = player;
    }

    public Player owner() {
      return this.owner;
    }

    public String getDisplayName() {
      return this.displayName;
    }

    public EntityType getEntityType() {
      return this.entityType;
    }
  }

  public static final class EntityAttribute {

    private final Map<Attribute, Double> entityAttributes;
    private final TriggerAttribute triggerAttribute;
    private final AmmoAttribute ammoAttribute;

    public EntityAttribute(final TriggerAttribute trigger, final AmmoAttribute ammo, final Map<Attribute, Double> attribute) {
      this.triggerAttribute = trigger;
      this.ammoAttribute = ammo;
      this.entityAttributes = attribute;
    }

    private void apply(final Entity entity) {
      final LivingEntity livingEntity = (LivingEntity) entity;

      this.entityAttributes.forEach((attribute, value) -> {
        livingEntity.registerAttribute(attribute);

        final AttributeInstance attributeInstance = livingEntity.getAttribute(attribute);
        if (attributeInstance == null)
          throw new RuntimeException("Failed to retrieve Attribute for entity!");

        attributeInstance.setBaseValue(value);
      });
    }

    public TriggerAttribute triggerAttribute() {
      return this.triggerAttribute;
    }

    public AmmoAttribute ammoAttribute() {
      return this.ammoAttribute;
    }

  }
}
