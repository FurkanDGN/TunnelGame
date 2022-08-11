package me.dantero.tunnelgame.common.game.configuration;

import org.bukkit.Location;

public class GameConfigurationBuilder {

  private Location spawnLocation;
  private LevelConfiguration levelConfiguration;

  public GameConfigurationBuilder setSpawnLocation(Location spawnLocation) {
    this.spawnLocation = spawnLocation;
    return this;
  }

  public GameConfigurationBuilder setLevelConfiguration(LevelConfiguration levelConfiguration) {
    this.levelConfiguration = levelConfiguration;
    return this;
  }

  public GameConfiguration build() {
    return new GameConfiguration(this.spawnLocation, this.levelConfiguration);
  }
}