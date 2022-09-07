package me.dantero.tunnelgame.common.game.configuration.component.entity.action.impl;

import me.dantero.tunnelgame.common.Constants;
import me.dantero.tunnelgame.common.game.configuration.ModifiedEntity;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Furkan Doğan
 */
public class SpawnAction extends AbstractAction {

  private final Map<EntityType, Integer> entityMap = new HashMap<>();
  private String target;

  @Override
  protected void init(ConfigurationSection section) {
    this.target = section.getString("target");
    List<String> entities = section.getStringList("entities");
    this.parseEntities(entities);
  }

  @Override
  public void onPlayerMove(ModifiedEntity modifiedEntity, PlayerMoveEvent event) {
    this.handle(modifiedEntity, event.getPlayer());
  }

  @Override
  protected void onPlayerAttackEntity(ModifiedEntity modifiedEntity, EntityDamageByEntityEvent event) {
    this.handle(modifiedEntity, (Player) event.getDamager());
  }

  @Override
  protected void onEntityAttackPlayer(ModifiedEntity modifiedEntity, EntityDamageByEntityEvent event) {
    this.handle(modifiedEntity, (Player) event.getEntity());
  }

  private void handle(ModifiedEntity modifiedEntity, Player player) {
    final LivingEntity livingEntity = (LivingEntity) modifiedEntity.entity();
    int entityId = livingEntity.getEntityId();

    final Location entityLocation = livingEntity.getLocation();
    final Location playerLocation = player.getLocation();

    Player owner = modifiedEntity.entityModifier().owner();
    String ownerName = owner != null ? owner.getName() : null;

    if (this.target.equalsIgnoreCase("SELF")) {
      this.spawnEntities(entityLocation, ownerName, entityId);
    } else {
      this.spawnEntities(playerLocation, ownerName, entityId);
    }
  }

  @Override
  protected long getPeriod() {
    return 20000L;
  }

  private void spawnEntities(Location location, String owner, int root) {
    this.entityMap.forEach((entityType, count) -> {
      for (int i = 0; i < count; i++) {
        if (entityType.equals(EntityType.LIGHTNING)) {
          location.getWorld().strikeLightning(location);
        } else {
          Entity entity = location.getWorld().spawnEntity(location, entityType, SpawnReason.CUSTOM);
          PersistentDataContainer persistentDataContainer = entity.getPersistentDataContainer();
          persistentDataContainer.set(Constants.ROOT_KEY, PersistentDataType.INTEGER, root);

          if (owner != null) {
            persistentDataContainer.set(Constants.OWNER_KEY, PersistentDataType.STRING, owner);
          }
        }
      }
    });
  }

  private void parseEntities(List<String> entities) {
    entities.forEach(line -> {
      String[] raw = line.split(":");
      String entityNameRaw = raw[0];
      String countRaw = raw[1];
      EntityType entityType = EntityType.valueOf(entityNameRaw);
      int count = Integer.parseInt(countRaw);
      this.entityMap.put(entityType, count);
    });
  }
}
