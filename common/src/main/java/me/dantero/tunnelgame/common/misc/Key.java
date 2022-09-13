package me.dantero.tunnelgame.common.misc;

import org.jetbrains.annotations.NotNull;

/**
 * an interface to determine key definitions.
 *
 * @param <K> type of the key.
 */
public interface Key<K> {

  /**
   * obtains the key.
   *
   * @return key.
   */
  @NotNull
  K key();
}
