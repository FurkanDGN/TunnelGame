package com.github.furkandgn.tunnelgame.plugin.manager;

import com.gmail.furkanaxx34.dlibrary.bukkit.scoreboard.BukkitScoreboard;
import com.gmail.furkanaxx34.dlibrary.bukkit.scoreboard.BukkitScoreboardSender;
import com.gmail.furkanaxx34.dlibrary.replaceable.RpString;
import com.gmail.furkanaxx34.dlibrary.scoreboard.Board;
import com.gmail.furkanaxx34.dlibrary.scoreboard.line.Line;
import com.github.furkandgn.tunnelgame.common.config.ConfigFile;
import com.github.furkandgn.tunnelgame.common.game.Session;
import com.github.furkandgn.tunnelgame.common.manager.ScoreboardManager;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Furkan Doğan
 */
public class DefaultScoreboardManager implements ScoreboardManager {

  @NotNull
  private final Plugin plugin;

  @Nullable
  private Board<Player> board;

  public DefaultScoreboardManager(@NotNull Plugin plugin) {
    this.plugin = plugin;
  }

  @Override
  public void setup(Session session) {
    BukkitScoreboard bukkitScoreboard = BukkitScoreboard.create(this.plugin, 20);
    BukkitScoreboardSender sender = bukkitScoreboard.getSender();

    List<String> rawLines = ConfigFile.scoreboardLines.build();
    List<Line<Player>> lines = this.convert(rawLines, session);

    this.board = Board.newBuilder(Player.class)
      .setId("tg-" + session.sessionId())
      .setScoreboardSender(sender)
      .setTitleLine(Line.immutable(ConfigFile.scoreboardTitle.build()))
      .setLines(lines)
      .addDynamicObserverList(() -> session.getSessionContext().getPlayers())
      .build();
    bukkitScoreboard.setup();
    this.board.start();
  }

  @Override
  public void stop() {
    Optional.ofNullable(this.board)
      .ifPresent(Board::close);
  }

  private List<Line<Player>> convert(List<String> rawLines, Session session) {
    return rawLines.stream()
      .map(raw -> this.convertLine(raw, session))
      .collect(Collectors.toList());
  }

  private Line<Player> convertLine(String line, Session session) {
    return Line.dynamic(player -> RpString.from(line)
      .regex("%player%", "%points%", "%level%")
      .build(
        Map.entry("%player%", player::getName),
        Map.entry("%points%", () -> String.valueOf(session.getSessionContext().getPointManager().getPoints(player))),
        Map.entry("%level%", () -> String.valueOf(session.getSessionContext().getCurrentLevel().get()))
      ));
  }
}
