package me.dantero.tunnelgame.common.util;

import me.dantero.tunnelgame.common.game.Session;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

public final class SessionListener<T extends Event> {

  @NotNull
  private final BiConsumer<T, Session> consumer;

  @NotNull
  private final EventPriority eventPriority;

  @NotNull
  private final Function<T, Optional<Session>> function;

  @NotNull
  private final Predicate<T> predicate;

  @NotNull
  private final Class<T> tClass;

  public SessionListener(@NotNull Class<T> tClass,
                         @NotNull Function<T, Optional<Session>> function,
                         @NotNull Predicate<T> predicate,
                         @NotNull BiConsumer<T, Session> consumer) {
    this(tClass, function, predicate, consumer, EventPriority.NORMAL);
  }

  public SessionListener(@NotNull Class<T> tClass,
                         @NotNull Function<T, Optional<Session>> function,
                         @NotNull Predicate<T> predicate,
                         @NotNull BiConsumer<T, Session> consumer,
                         @NotNull EventPriority eventPriority) {
    this.tClass = tClass;
    this.function = function;
    this.predicate = predicate;
    this.consumer = consumer;
    this.eventPriority = eventPriority;
  }

  @SuppressWarnings("unchecked")
  public void register(@NotNull final Plugin plugin) {
    Bukkit.getServer().getPluginManager().registerEvent(
      this.tClass,
      new Listener() {
      },
      this.eventPriority,
      (listener, event) -> {
        if (event.getClass().equals(this.tClass)) {
          Optional<Session> sessionOptional = this.function.apply((T) event);
          if (sessionOptional.isPresent() && this.predicate.test((T) event)) {
            this.consumer.accept((T) event, sessionOptional.get());
          }
        }
      },
      plugin
    );
  }
}
