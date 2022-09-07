package me.dantero.tunnelgame.common.game.configuration;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Furkan Doğan
 */
public record LevelConfiguration(Map<Integer, Integer> levelLength, Map<Integer, List<ModifiedEntitySetting>> levelEntities) {

  @Override
  public Map<Integer, List<ModifiedEntitySetting>> levelEntities() {
    return Collections.unmodifiableMap(this.levelEntities);
  }
}
