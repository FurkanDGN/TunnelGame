package me.dantero.tunnelgame.plugin.listeners;

import com.gmail.furkanaxx34.dlibrary.bukkit.utils.CooldownUtil;
import me.dantero.tunnelgame.common.Constants;
import me.dantero.tunnelgame.common.config.LanguageFile;
import me.dantero.tunnelgame.common.config.SignsFile;
import me.dantero.tunnelgame.common.manager.SignManager;
import me.dantero.tunnelgame.common.misc.ServerInfos;
import me.dantero.tunnelgame.common.misc.Servers;
import me.dantero.tunnelgame.common.misc.SessionAddress;
import me.dantero.tunnelgame.common.proto.GameState;
import me.dantero.tunnelgame.common.proto.Session;
import me.dantero.tunnelgame.common.util.BungeeUtil;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * @author Furkan Doğan
 */
public class LobbyListeners implements Listener {

  private final SignManager signManager;

  public LobbyListeners(SignManager signManager) {
    this.signManager = signManager;
  }

  @EventHandler
  public void onSignChange(SignChangeEvent event) {
    if (!event.getPlayer().hasPermission(Constants.ADMIN_PERMISSION)) return;

    String[] lines = event.getLines();
    Sign sign = (Sign) event.getBlock().getState();

    String line1 = lines[0];
    String line2 = lines[1];
    if (line1.equalsIgnoreCase(Constants.SIGN_FIRST_LINE) && !line2.isBlank()) {
      this.signManager.save(sign, line2);
    }
  }

  @EventHandler
  public void onSignBreak(BlockBreakEvent event) {
    Location location = event.getBlock().getLocation();
    if (SignsFile.signLocations.containsValue(location)) {
      if (!event.getPlayer().hasPermission(Constants.ADMIN_PERMISSION)) {
        event.setCancelled(true);
      } else {
        this.signManager.delete(location);
      }
    }
  }

  @EventHandler
  public void onSignInteract(PlayerInteractEvent event) {
    Block clickedBlock = event.getClickedBlock();
    if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
    if (clickedBlock == null) return;
    if (!clickedBlock.getType().name().endsWith("WALL_SIGN")) return;

    Player player = event.getPlayer();

    Location location = clickedBlock.getLocation();
    SignsFile.findFromLocation(location)
      .ifPresent(entry -> {
        SessionAddress sessionAddress = entry.getKey();
        String server = sessionAddress.server();
        int sessionId = sessionAddress.sessionId();
        ServerInfos.getSession(server, sessionId)
          .ifPresent(session -> this.joinPlayer(session, player, sessionId));
      });
  }

  private void joinPlayer(Session session, Player player, int sessionId) {
    if (session.getPlayerCount() < session.getMaxPlayerCount()) {
      if (session.getGameState() == GameState.GAME_STATE_WAITING || session.getGameState() == GameState.GAME_STATE_STARTING) {
        Servers.LIST.stream()
          .filter(spigotServer -> spigotServer.sessions().contains(sessionId))
          .findFirst()
          .ifPresent(spigotServer -> BungeeUtil.sendPlayer(player, spigotServer.name(), sessionId));
      } else {
        boolean check = CooldownUtil.check(player, "sign-interact", 5000L);
        if (check) {
          player.sendMessage(LanguageFile.serverNotReady.build());
        }
      }
    } else {
      boolean check = CooldownUtil.check(player, "sign-interact", 5000L);
      if (check) {
        player.sendMessage(LanguageFile.serverIsFull.build());
      }
    }
  }

}
