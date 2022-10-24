package me.dantero.tunnelgame.common.redis;

import com.google.protobuf.GeneratedMessageV3;
import me.dantero.tunnelgame.common.misc.Key;
import me.dantero.tunnelgame.common.proto.ServerMessage;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;

/**
 * a record class that represents subscriptions.
 *
 * @param consumer the consumer.
 * @param template the template.
 * @param <T> type of the server message.
 */
@SuppressWarnings("unchecked")
record Subscription<T extends GeneratedMessageV3>(
  @NotNull BiConsumer<ServerMessage, T> consumer,
  @NotNull T template
) implements Key<String> {

  @NotNull
  @Override
  public String key() {
    return this.template.getClass().getSimpleName();
  }

  /**
   * runs when a
   *
   * @param serverMessage the server message to run.
   */
  void onMessage(@NotNull final ServerMessage serverMessage) throws Exception {
    this.consumer.accept(
      serverMessage,
      (T) this.template.getParserForType().parseFrom(serverMessage.getData())
    );
  }
}
