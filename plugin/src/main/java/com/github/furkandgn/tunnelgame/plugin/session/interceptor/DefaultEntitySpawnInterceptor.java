package com.github.furkandgn.tunnelgame.plugin.session.interceptor;

import com.github.furkandgn.tunnelgame.common.game.configuration.ModifiedEntity;
import com.github.furkandgn.tunnelgame.common.game.interceptor.EntitySpawnInterceptor;
import org.bukkit.entity.Entity;

import java.util.List;
import java.util.Set;

/**
 * @author Furkan Doğan
 */
public class DefaultEntitySpawnInterceptor implements EntitySpawnInterceptor {

  private final Set<Integer> entityIds;
  private final List<ModifiedEntity> currentEntities;

  public DefaultEntitySpawnInterceptor(Set<Integer> entityIds,
                                       List<ModifiedEntity> currentEntities) {
    this.entityIds = entityIds;
    this.currentEntities = currentEntities;
  }

  @Override
  public ModifiedEntity preEntitySpawn(ModifiedEntity modifiedEntity) {
    return modifiedEntity;
  }

  @Override
  public void postEntitySpawn(Entity entity,
                              ModifiedEntity modifiedEntity) {
    this.entityIds.add(entity.getEntityId());
    this.currentEntities.add(modifiedEntity);
  }
}
