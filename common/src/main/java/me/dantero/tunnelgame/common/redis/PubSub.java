package me.dantero.tunnelgame.common.redis;

import com.google.protobuf.GeneratedMessageV3;
import io.lettuce.core.pubsub.RedisPubSubAdapter;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import lombok.Setter;
import lombok.experimental.NonFinal;
import me.dantero.tunnelgame.common.misc.Protobuf;
import me.dantero.tunnelgame.common.misc.Servers;
import me.dantero.tunnelgame.common.misc.Topics;
import me.dantero.tunnelgame.common.proto.ServerMessage;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * @author Furkan Doğan
 */
public final class PubSub extends RedisPubSubAdapter<byte[], byte[]> {

  /**
   * the closed.
   */
  AtomicBoolean closed = new AtomicBoolean();

  /**
   * the connection.
   */
  @NotNull
  StatefulRedisPubSubConnection<byte[], byte[]> connection;

  /**
   * the subscribes.
   */
  Registry<String, Subscription<?>> subscribes = new Registry<>();

  /**
   * the topic.
   */
  byte @NotNull [] topic;

  /**
   * the filter channel.
   */
  @Setter
  @NotNull
  @NonFinal
  Predicate<byte[]> filterChannel = bytes -> true;

  /**
   * the message filters.
   */
  @NotNull
  @NonFinal
  Predicate<ServerMessage> messageFilters = message -> true;

  /**
   * ctor.
   *
   * @param topic the topic.
   */
  public PubSub(
    final byte @NotNull [] topic
  ) {
    this.topic = topic.clone();
    this.connection = Redis.pubSubSync();
    this.connection.addListener(this);
    this.connection.async().subscribe(this.topic);
  }

  /**
   * adds the message filter.
   *
   * @param filter the filter to add.
   *
   * @return {@code this} for the chain.
   */
  @NotNull
  public PubSub addMessageFilter(
    @NotNull final Predicate<ServerMessage> filter
  ) {
    this.messageFilters = this.messageFilters.and(filter);
    return this;
  }

  public void close() {
    this.closed.set(true);
    this.connection.closeAsync();
  }

  public boolean closed() {
    return this.closed.get();
  }

  /**
   * ignores receiving server message from itself.
   *
   * @return {@code this} for the chain.
   */
  @NotNull
  public PubSub ignoreFromItself() {
    return this.addMessageFilter(message -> !message.getSource().equals(Servers.getServerName()));
  }

  @Override
  public void message(
    final byte[] channel,
    final byte[] message
  ) {
    if (!this.canReceive(channel)) {
      return;
    }
    try {
      final var serverMessage = ServerMessage.parseFrom(message);
      if (!Servers.is(serverMessage.getTarget())) {
        return;
      }
      if (!this.messageFilters.test(serverMessage)) {
        return;
      }
      final var type = serverMessage.getType();
      final var subscription = this.subscribes.get(type);
      if (subscription.isEmpty()) {
        return;
      }
      subscription.get().onMessage(serverMessage);
    } catch (final Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * sends the message.
   *
   * @param message the message to send.
   *
   * @return sent server message.
   */
  @NotNull
  @Contract("_ -> param1")
  public ServerMessage send(
    @NotNull final ServerMessage message
  ) {
    try (final var connection = Redis.pubSubSync()) {
      connection.sync().publish(this.topic, message.toByteArray());
    }
    return message;
  }

  /**
   * sends the message.
   *
   * @param target the target to send.
   * @param message the message to send.
   *
   * @return sent server message.
   */
  @NotNull
  public ServerMessage send(
    @NotNull final String target,
    @NotNull final GeneratedMessageV3 message
  ) {
    return this.send(Protobuf.createServerMessage(target, message));
  }

  /**
   * sends the message.
   *
   * @param message the message to send.
   *
   * @return sent server message.
   */
  @NotNull
  public CompletionStage<ServerMessage> sendAsync(
    @NotNull final ServerMessage message
  ) {
    return Redis.pubSubAsync().thenApply(connection -> {
      try (connection) {
        connection.sync().publish(this.topic, message.toByteArray());
      }
      return message;
    });
  }

  /**
   * sends the message.
   *
   * @param target the target to send.
   * @param message the message to send.
   *
   * @return sent server message.
   */
  @NotNull
  public CompletionStage<ServerMessage> sendAsync(
    @NotNull final String target,
    @NotNull final GeneratedMessageV3 message
  ) {
    return this.sendAsync(Protobuf.createServerMessage(target, message));
  }

  /**
   * subscribes to the template.
   *
   * @param template the template to subscribe.
   * @param coming the coming to subscribe.
   * @param <T> type of the template.
   */
  public <T extends GeneratedMessageV3> void subscribe(
    @NotNull final T template,
    @NotNull final BiConsumer<ServerMessage, T> coming
  ) {
    this.subscribes.register(new Subscription<>(coming, template));
  }

  /**
   * subscribes to the template.
   *
   * @param template the template to subscribe.
   * @param coming the coming to subscribe.
   * @param <T> type of the template.
   */
  public <T extends GeneratedMessageV3> void subscribe(
    @NotNull final T template,
    @NotNull final Consumer<T> coming
  ) {
    this.subscribe(template, (message, t) -> coming.accept(t));
  }

  /**
   * unsubscribes from the key.
   *
   * @param key the key to unsubscribe.
   */
  public void unsubscribe(
    @NotNull final String key
  ) {
    this.subscribes.unregister(key);
  }

  /**
   * checks if the topic can receive here.
   *
   * @param topic the topic to check.
   *
   * @return {@code true} if the topic can receive here.
   */
  private boolean canReceive(final byte @NotNull [] topic) {
    return this.filterChannel.test(topic) &&
      Arrays.equals(topic, Topics.ANY) || Arrays.equals(topic, this.topic);
  }

}
