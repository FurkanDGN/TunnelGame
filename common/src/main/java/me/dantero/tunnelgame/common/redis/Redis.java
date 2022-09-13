package me.dantero.tunnelgame.common.redis;

import io.lettuce.core.ConnectionFuture;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import lombok.experimental.UtilityClass;
import me.dantero.tunnelgame.common.config.ConfigFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * @author Furkan Doğan
 */
@UtilityClass
public class Redis {

  /**
   * the client.
   */
  @Nullable
  private RedisClient client;

  /**
   * the uri.
   */
  @Nullable
  private RedisURI uri;

  /**
   * obtains the redis client.
   *
   * @return redis client.
   */
  @NotNull
  public RedisClient get() {
    return Objects.requireNonNull(Redis.client, "init redis first!");
  }

  /**
   * initiates the redis.
   */
  public void init() {
    Redis.init(ConfigFile.RedisConfig.redisHost, ConfigFile.RedisConfig.redisUsername, ConfigFile.RedisConfig.redisPassword);
  }

  /**
   * initiates the redis.
   *
   * @param host the master id to init.
   * @param username the username to init.
   * @param password the password to init.
   */
  public void init(@NotNull final String host, @Nullable final String username, @NotNull final String password) {
    final var builder = RedisURI.Builder.sentinel(host);
    if (username == null) {
      builder.withPassword(password.toCharArray());
    } else {
      builder.withAuthentication(username, password);
    }
    Redis.uri = builder.build();
    Redis.client = RedisClient.create(Redis.uri);
  }

  /**
   * connects to the pub sub.
   *
   * @return pub sub connection.
   */
  @NotNull
  public ConnectionFuture<StatefulRedisPubSubConnection<byte[], byte[]>> pubSubAsync() {
    return Redis.get().connectPubSubAsync(ByteArrayCodec.INSTANCE, Redis.uri);
  }

  /**
   * connects to the pub sub.
   *
   * @return pub sub connection.
   */
  @NotNull
  public StatefulRedisPubSubConnection<byte[], byte[]> pubSubSync() {
    return Redis.get().connectPubSub(ByteArrayCodec.INSTANCE);
  }
}
