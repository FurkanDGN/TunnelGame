package com.github.furkandgn.tunnelgame.common.redis;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import com.github.furkandgn.tunnelgame.common.misc.Key;
import org.jetbrains.annotations.NotNull;

/**
 * a class that represents registries.
 *
 * @param <K> type of the key.
 * @param <V> type of teh value.
 */
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class Registry<K, V extends Key<K>> {

  /**
   * the values.
   */
  @NotNull
  Map<K, V> values;

  /**
   * ctor.
   */
  public Registry() {
    this(new ConcurrentHashMap<>());
  }

  /**
   * obtains the all.
   *
   * @return all.
   */
  @NotNull
  public Map<K, V> all() {
    return Collections.unmodifiableMap(this.values);
  }

  /**
   * gets the value by key.
   *
   * @param key the key to get.
   *
   * @return value.
   */
  @NotNull
  public Optional<V> get(@NotNull final K key) {
    return Optional.ofNullable(this.values.get(key));
  }

  /**
   * obtains the keys.
   *
   * @return keys.
   */
  @NotNull
  public Set<K> keys() {
    return Collections.unmodifiableSet(this.values.keySet());
  }

  /**
   * registers the value.
   *
   * @param value the value to register.
   */
  public void register(@NotNull final V value) {
    this.values.put(value.key(), value);
  }

  /**
   * unregisters the key.
   *
   * @param key the key to unregister.
   */
  public void unregister(@NotNull final K key) {
    this.values.remove(key);
  }

  /**
   * unregisters the value.
   *
   * @param value the value to unregister.
   */
  public void unregister(@NotNull final V value) {
    this.unregister(value.key());
  }

  /**
   * obtains the values.
   *
   * @return values.
   */
  @NotNull
  public Collection<V> values() {
    return Collections.unmodifiableCollection(this.values.values());
  }
}
