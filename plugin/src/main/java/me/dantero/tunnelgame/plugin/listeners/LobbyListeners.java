package me.dantero.tunnelgame.plugin.listeners;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * @author Furkan Doğan
 */
public class LobbyListeners implements Listener {

  @EventHandler
  public void onSignClick(PlayerInteractEvent event) {
    if (event.getClickedBlock() == null || event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
    Material type = event.getClickedBlock().getType();

  }

}
