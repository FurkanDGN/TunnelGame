package me.dantero.tunnelgame.common.misc;

import java.util.Objects;

/**
 * @author Furkan Doğan
 */
public record SessionAddress(String server, int sessionId) {

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || this.getClass() != o.getClass()) return false;
    SessionAddress that = (SessionAddress) o;
    return this.sessionId == that.sessionId && this.server.equals(that.server);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.server, this.sessionId);
  }
}
