package com.github.furkandgn.tunnelgame.common.misc;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Set;

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
  Set<Integer> sessions;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || this.getClass() != o.getClass()) return false;
    SpigotServer that = (SpigotServer) o;
    return this.name.equals(that.name) && this.sessions.equals(that.sessions);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.name, this.sessions);
  }
}
