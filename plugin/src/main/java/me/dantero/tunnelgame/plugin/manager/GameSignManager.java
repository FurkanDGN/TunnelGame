package me.dantero.tunnelgame.plugin.manager;

import com.gmail.furkanaxx34.dlibrary.bukkit.utils.TaskUtilities;
import me.dantero.tunnelgame.common.Constants;
import me.dantero.tunnelgame.common.config.ConfigFile;
import me.dantero.tunnelgame.common.config.LanguageFile;
import me.dantero.tunnelgame.common.config.SignsFile;
import me.dantero.tunnelgame.common.manager.SignManager;
import me.dantero.tunnelgame.common.misc.ServerInfos;
import me.dantero.tunnelgame.common.misc.SessionAddress;
import me.dantero.tunnelgame.common.proto.GameState;
import me.dantero.tunnelgame.common.proto.Session;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.regex.Matcher;

/**
 * @author Furkan Doğan
 */
public class GameSignManager implements SignManager {

  private final Map<SessionAddress, Sign> signs = new ConcurrentHashMap<>();

  @Override
  public void init() {
    SignsFile.signLocations.entrySet()
      .stream()
      .map(entry -> new SimpleEntry<>(entry.getKey(), entry.getValue().getBlock()))
      .filter(entry -> entry.getValue().getType().name().endsWith("_WALL_SIGN"))
      .map(entry -> new SimpleEntry<>(entry.getKey(), (Sign) entry.getValue().getState()))
      .filter(entry -> entry.getValue().getLine(0).equalsIgnoreCase(ConfigFile.lobbySign.build().get(0)))
      .forEach(entry -> this.signs.put(entry.getKey(), entry.getValue()));

    TaskUtilities.asyncTimerLater(20L * 3L, 10L, bukkitRunnable -> {
      this.signs.forEach((sessionAddress, sign) -> ServerInfos.getSession(sessionAddress.server(), sessionAddress.sessionId())
        .ifPresentOrElse(session -> {
          List<String> lines = ConfigFile.lobbySign.build(
            new SimpleEntry<>("%cur-pl%", () -> String.valueOf(session.getPlayerCount())),
            new SimpleEntry<>("%max-pl%", () -> String.valueOf(session.getMaxPlayerCount())),
            new SimpleEntry<>("%state%", () -> this.getStringValue(session.getGameState())),
            new SimpleEntry<>("%session-id%", () -> String.valueOf(session.getId())),
            new SimpleEntry<>("%server-suffix%", sessionAddress::server)
          );

          for (int i = 0; i < 4; i++) {
            sign.setLine(i, lines.get(i));
          }
        }, () -> {
          List<String> lines = ConfigFile.lobbySign.build(
            new SimpleEntry<>("%cur-pl%", () -> "NaN"),
            new SimpleEntry<>("%max-pl%", () -> "NaN"),
            new SimpleEntry<>("%state%", () -> LanguageFile.offline.build()),
            new SimpleEntry<>("%session-id%", () -> String.valueOf(sessionAddress.sessionId())),
            new SimpleEntry<>("%server-suffix%", sessionAddress::server)
          );

          for (int i = 0; i < 4; i++) {
            sign.setLine(i, lines.get(i));
          }
        }));

      TaskUtilities.syncTimer(20L * 5L, (Consumer<BukkitRunnable>) bukkitRunnable1 -> this.signs.values().forEach(BlockState::update));
    });
  }

  @Override
  public void save(Sign sign, String line2) {
    Matcher matcher = Constants.SESSION_ADDRESS_PATTERN.matcher(line2);
    if (!matcher.matches()) return;

    String server = matcher.group("server");
    String id = matcher.group("id");
    int sessionId = Integer.parseInt(id);
    SessionAddress sessionAddress = new SessionAddress(server, sessionId);

    Optional<Session> sessionOptional = ServerInfos.getSession(sessionAddress.server(), sessionAddress.sessionId());
    if (sessionOptional.isPresent()) {
      this.signs.put(sessionAddress, sign);
      SignsFile.save(server, sessionId, sign.getLocation());
    }
  }

  @Override
  public void delete(Location location) {
    SignsFile.findFromLocation(location)
      .ifPresent(entry -> {
        SessionAddress sessionAddress = entry.getKey();
        String server = sessionAddress.server();
        int sessionId = sessionAddress.sessionId();
        SignsFile.save(server, sessionId, null);
        Sign removed = this.signs.remove(sessionAddress);
        if (removed != null) {
          removed.getBlock().setType(Material.AIR);
        }
      });
  }

  private String getStringValue(GameState gameState) {
    return switch (gameState) {
      case GAME_STATE_IN_GAME -> LanguageFile.inGame.build();
      case GAME_STATE_STARTING -> LanguageFile.starting.build();
      case GAME_STATE_WAITING -> LanguageFile.waiting.build();
      case GAME_STATE_ENDED -> LanguageFile.ended.build();
      case GAME_STATE_ROLLBACK -> LanguageFile.rollback.build();
      case GAME_STATE_BROKEN -> LanguageFile.broken.build();
      case UNRECOGNIZED -> "Unknown";
    };
  }
}
