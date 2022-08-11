package me.dantero.tunnelgame.common.game.configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LevelConfigurationBuilder {

  private final Map<Integer, List<ModifiedEntitySetting>> levelEntities = new HashMap<>();
  private final Map<Integer, Long> levelLength = new HashMap<>();

  public LevelConfigurationBuilder addLevelEntity(Integer level, ModifiedEntitySetting modifiedEntitySetting) {
    List<ModifiedEntitySetting> list = this.levelEntities.getOrDefault(level, new ArrayList<>());
    list.add(modifiedEntitySetting);
    this.levelEntities.put(level, list);
    return this;
  }

  public LevelConfigurationBuilder addLevelLength(Integer level, long length) {
    this.levelLength.put(level, length);
    return this;
  }

  public LevelConfiguration createLevelConfiguration() {
    return new LevelConfiguration(this.levelLength, this.levelEntities);
  }
}