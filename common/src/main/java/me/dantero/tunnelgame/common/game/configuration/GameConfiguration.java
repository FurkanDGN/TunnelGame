package me.dantero.tunnelgame.common.game.configuration;

import org.bukkit.Location;

/**
 * @author Furkan Doğan
 */
public record GameConfiguration(Location spawnLocation, LevelConfiguration levelConfiguration) {
}