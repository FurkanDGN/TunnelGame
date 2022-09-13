package me.dantero.tunnelgame.common.misc;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import me.dantero.tunnelgame.common.game.state.GameState;
import org.jetbrains.annotations.NotNull;

/**
 * @author Furkan Doğan
 */
@Getter
@RequiredArgsConstructor
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class SpigotServer {

  /**
   * the name.
   */
  @NotNull
  String name;

  @NotNull
  GameState gameState;
}
