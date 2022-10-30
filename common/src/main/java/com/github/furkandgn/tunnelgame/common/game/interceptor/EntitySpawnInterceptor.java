package com.github.furkandgn.tunnelgame.common.game.interceptor;

import com.github.furkandgn.tunnelgame.common.game.configuration.ModifiedEntity;
import org.bukkit.entity.Entity;

/**
 * @author Furkan Doğan
 */
public interface EntitySpawnInterceptor {

  ModifiedEntity preEntitySpawn(ModifiedEntity modifiedEntity);

  void postEntitySpawn(Entity entity, ModifiedEntity modifiedEntity);
}
