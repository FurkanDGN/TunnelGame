package com.github.furkandgn.tunnelgame.common.game.configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LevelConfigurationBuilder {

  private final Map<Integer, List<ModifiedEntitySetting>> levelEntities = new HashMap<>();
  private final Map<Integer, Integer> levelLength = new HashMap<>();

  public LevelConfigurationBuilder addLevelEntity(int level, ModifiedEntitySetting modifiedEntitySetting) {
    List<ModifiedEntitySetting> list = this.levelEntities.getOrDefault(level, new ArrayList<>());
    list.add(modifiedEntitySetting);
    this.levelEntities.put(level, list);
    return this;
  }

  public LevelConfigurationBuilder addLevelLength(int level, int length) {
    this.levelLength.put(level, length);
    return this;
  }

  public LevelConfiguration createLevelConfiguration() {
    return new LevelConfiguration(this.levelLength, this.levelEntities);
  }
}