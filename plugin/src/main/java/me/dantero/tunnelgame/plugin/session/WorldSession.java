package me.dantero.tunnelgame.plugin.session;

import me.dantero.tunnelgame.common.config.ConfigFile;
import me.dantero.tunnelgame.common.game.Level;
import me.dantero.tunnelgame.common.game.Session;
import me.dantero.tunnelgame.common.game.state.JoinResultState;
import me.dantero.tunnelgame.common.manager.WorldManager;
import me.dantero.tunnelgame.common.util.FilenameUtil;
import me.dantero.tunnelgame.plugin.session.manager.MapManager;
import me.dantero.tunnelgame.plugin.session.manager.PlayerStore;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author Furkan Doğan
 */
public class WorldSession implements Session {

  private static final AtomicInteger SESSION_ID = new AtomicInteger(0);

  private final List<Level> levels = new ArrayList<>();
  private final Set<UUID> players = new HashSet<>();
  private final MapManager mapManager;
  private final PlayerStore playerStore;

  public WorldSession(File worldRecoverPath,
                      WorldManager worldManager) {
    File worldPath = this.buildWorldPath(worldRecoverPath);
    this.mapManager = new MapManager(worldPath, worldRecoverPath, worldManager);
    this.playerStore = new PlayerStore();
  }

  @Override
  public void prepare() {
    this.mapManager.reloadMap();
  }

  @Override
  public void start() {

  }

  @Override
  public void stop() {
    SESSION_ID.decrementAndGet();
    this.resetPlayers();
  }

  @Override
  public JoinResultState tryJoinPlayer(Player player) {
    UUID uniqueId = player.getUniqueId();

    if (this.players.size() >= ConfigFile.maxPlayers) {
      return JoinResultState.GAME_FULL;
    }
    if (this.players.contains(uniqueId)) {
      return JoinResultState.ALREADY_IN_GAME;
    }

    this.joinPlayer(player);
    return JoinResultState.SUCCESSFUL;
  }

  @Override
  public boolean isInGame(Player player) {
    return this.players.contains(player.getUniqueId());
  }

  @Override
  public boolean isStarted() {
    return false;
  }

  @Override
  public Set<Player> players() {
    return this.players.stream()
      .map(Bukkit::getPlayer)
      .collect(Collectors.toSet());
  }

  private void joinPlayer(Player player) {
    this.players.add(player.getUniqueId());
    this.playerStore.savePlayer(player);
  }

  private File buildWorldPath(File file) {
    String worldName = FilenameUtil.withoutExtension(file);
    String fileName = String.format("%s-%03d", worldName, SESSION_ID.incrementAndGet());
    return new File(Bukkit.getWorldContainer(), fileName);
  }

  private void resetPlayers() {
    this.players.clear();
    this.players().forEach(player -> {
      this.playerStore.resetPlayer(player);
      player.kickPlayer("Game stopped.");
    });
  }
}
