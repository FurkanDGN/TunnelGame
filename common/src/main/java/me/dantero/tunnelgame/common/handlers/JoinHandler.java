package me.dantero.tunnelgame.common.handlers;

import me.dantero.tunnelgame.common.proto.JoinRequest;
import me.dantero.tunnelgame.common.proto.ServerMessage;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Optional;

/**
 * @author Furkan Doğan
 */
public interface JoinHandler {

  void handle(Player player);

  Optional<Location> getSpawnLocation(Player player);

  void handleRequest(ServerMessage serverMessage, JoinRequest joinRequest);
}
