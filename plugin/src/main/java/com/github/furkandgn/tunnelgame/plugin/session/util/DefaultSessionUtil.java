package com.github.furkandgn.tunnelgame.plugin.session.util;

import com.github.furkandgn.tunnelgame.common.config.ConfigFile;
import com.github.furkandgn.tunnelgame.common.config.LanguageFile;
import com.github.furkandgn.tunnelgame.common.game.Level;
import com.github.furkandgn.tunnelgame.common.game.Session;
import com.github.furkandgn.tunnelgame.common.game.SessionContext;
import com.github.furkandgn.tunnelgame.common.game.SessionUtil;
import com.github.furkandgn.tunnelgame.common.game.configuration.LevelConfiguration;
import com.github.furkandgn.tunnelgame.common.game.configuration.ModifiedEntity;
import com.github.furkandgn.tunnelgame.common.game.configuration.ModifiedEntitySetting;
import com.github.furkandgn.tunnelgame.common.game.interceptor.EntitySpawnInterceptor;
import com.github.furkandgn.tunnelgame.common.game.state.GameState;
import com.github.furkandgn.tunnelgame.common.manager.MapManager;
import com.github.furkandgn.tunnelgame.common.util.game.BungeeUtil;
import com.github.furkandgn.tunnelgame.common.util.game.LocationUtil;
import com.github.furkandgn.tunnelgame.plugin.level.TunnelLevel;
import com.gmail.furkanaxx34.dlibrary.bukkit.color.XColor;
import com.gmail.furkanaxx34.dlibrary.bukkit.location.RandomUtil;
import com.gmail.furkanaxx34.dlibrary.bukkit.utils.TaskUtilities;
import com.grinderwolf.swm.api.exceptions.UnknownWorldException;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * @author Furkan Doğan
 */
public class DefaultSessionUtil implements SessionUtil {

  private final Session session;
  private final SessionContext sessionContext;
  private final EntitySpawnInterceptor entitySpawnInterceptor;

  public DefaultSessionUtil(Session session,
                            EntitySpawnInterceptor entitySpawnInterceptor) {
    Objects.requireNonNull(session, "Session cannot be null");
    Objects.requireNonNull(entitySpawnInterceptor, "Entity spawn interceptor cannot be null");
    this.session = session;
    this.sessionContext = this.session.getSessionContext();
    this.entitySpawnInterceptor = entitySpawnInterceptor;
  }

  @Override
  public void startCountdown(int time, String messagePrefix, Consumer<Void> onEnd) {
    AtomicInteger countdown = new AtomicInteger(time);
    TaskUtilities.syncTimer(20, bukkitRunnable -> {
      if (this.sessionContext.getGameState() == GameState.ENDED || this.sessionContext.getPlayers().size() == 0) {
        this.sessionContext.setGameState(GameState.WAITING);
        bukkitRunnable.cancel();
        return;
      }

      int countdownInt = countdown.getAndDecrement();
      if (countdownInt <= 0) {
        try {
          onEnd.accept(null);
        } catch (Exception ex) {
          ex.printStackTrace();
        }
        bukkitRunnable.cancel();
      } else {
        String message = XColor.colorize(String.format("&a%s: %s", messagePrefix, countdownInt));
        this.sendTitle("&a", message);
        this.sendActionBar(message);
      }
    });
  }

  @Override
  public Location currentLevelSpawnPoint() {
    World world = this.getWorld();
    int currentLevel = this.sessionContext.getCurrentLevel().get();
    int totalLength = this.calculateLevelLength(currentLevel) + currentLevel - 1;
    Location spawnPoint = ConfigFile.getSpawnPoint();
    spawnPoint.setWorld(Objects.requireNonNull(world, "World is null"));
    int length = this.session.getLevelConfiguration().levelLength().get(currentLevel);
    Vector vector = ConfigFile.gameDirection.getDirection().multiply(totalLength - length);
    return spawnPoint.add(vector);
  }

  @Override
  public int calculateLevelLength(int level) {
    LevelConfiguration levelConfiguration = this.session.getLevelConfiguration();
    if (level <= 1) {
      return levelConfiguration.levelLength().getOrDefault(level, -1);
    } else {
      int length = levelConfiguration.levelLength().get(level);
      return length + this.calculateLevelLength(level - 1);
    }
  }

  @Override
  public void teleportPlayers(Location location) {
    this.peekPlayers(player -> player.teleport(LocationUtil.randomRotatedLocation(location)));
  }

  @Override
  public void sendTitle(String title, String subtitle) {
    this.peekPlayers(player -> player.sendTitle(XColor.colorize(title), XColor.colorize(subtitle), 15, 20, 3));
  }

  @Override
  public void sendActionBar(String message) {
    this.peekPlayers(player -> player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message)));
  }

  @Override
  public void peekPlayers(Consumer<Player> playerConsumer) {
    this.sessionContext.getPlayers()
      .stream()
      .filter(Objects::nonNull)
      .forEach(playerConsumer);
  }

  @Override
  public void teleportPlayersSpawn() {
    Location spawnPoint = ConfigFile.getSpawnPoint();
    spawnPoint.setWorld(Bukkit.getWorld(this.sessionContext.getWorldName()));
    this.teleportPlayers(spawnPoint);
  }

  @Override
  public Map<Integer, Level> buildLevels() {
    return this.session.getLevelConfiguration().levelLength()
      .entrySet()
      .stream()
      .map(entry -> new AbstractMap.SimpleEntry<>(entry.getKey(), new TunnelLevel(entry.getValue(), ConfigFile.lowestBlockHeight)))
      .collect(HashMap::new, (map, entry) -> map.put(entry.getKey(), entry.getValue()), Map::putAll);
  }

  @Override
  public void sendLobby(Player player) {
    UUID uniqueId = player.getUniqueId();
    String comingFrom = this.sessionContext.getComingFrom(uniqueId);
    if (comingFrom != null && !comingFrom.isBlank()) {
      BungeeUtil.sendPlayerWithoutMessage(player, comingFrom);
    } else {
      player.kickPlayer(LanguageFile.kickMessage.build());
    }
  }

  @Override
  public void kickPlayers() {
    World world = this.getWorld();
    if (world != null) {
      world.getPlayers().forEach(player -> player.kickPlayer(LanguageFile.kickMessage.build()));
    }
  }

  @Override
  public void startGameCountdown() {
    this.startCountdown(ConfigFile.startCountdown, LanguageFile.gameStarting.build(), unused -> this.startGame());
  }

  @Override
  public void startGame() {
    this.sessionContext.setGameState(GameState.IN_GAME);
    this.session.getInventoryManager().giveStarterKit();
    this.session.getLevelConfiguration().levelEntities().get(1).forEach(this::spawnEntity);
    String message = LanguageFile.gameStarted.build();
    this.sendActionBar(message);
    this.sendTitle("&b", message);
  }

  @Override
  public void prepareLevels(Map<Integer, Level> levels) {
    World world = this.getWorld();
    if (world == null) {
      return;
    }

    Location spawnPoint = ConfigFile.getSpawnPoint();
    spawnPoint.setWorld(world);
    BlockFace gameDirection = ConfigFile.gameDirection;

    for (Level level : levels.values()) {
      level.prepare(spawnPoint, gameDirection);
      int length = level.goalLength();
      Vector vector = gameDirection.getDirection();
      vector.multiply(length + 1);
      spawnPoint.add(vector);
    }
  }

  @Override
  public World getWorld() {
    MapManager mapManager = this.session.getMapManager();
    String worldName = mapManager.getWorldName();
    return Bukkit.getWorld(worldName);
  }

  @Override
  public void reloadWorld() {
    try {
      this.deleteWorld();
      this.session.prepare();
    } catch (Exception e) {
      this.sessionContext.setGameState(GameState.BROKEN);
      throw new RuntimeException("An error occurred. Status set to broken.", e);
    }
  }

  @Override
  public void loadWorld() {
    MapManager defaultMapManager = this.session.getMapManager();

    try {
      defaultMapManager.reloadMap();
    } catch (UnknownWorldException e) {
      defaultMapManager.cloneAndLoadWorld();
    } catch (Exception e) {
      this.sessionContext.setGameState(GameState.BROKEN);
      throw new RuntimeException(e);
    }
  }

  @Override
  public void spawnEntity(ModifiedEntitySetting modifiedEntitySetting) {
    for (int i = 0; i < modifiedEntitySetting.getCount(); i++) {
      ModifiedEntity modifiedEntity = modifiedEntitySetting.create(this.session.getMapManager().getWorldName());
      int level = this.sessionContext.getCurrentLevel().get();
      int length = this.calculateLevelLength(level);
      int levelLength = this.session.getLevelConfiguration().levelLength().get(level);
      Location spawn = ConfigFile.getSpawnPoint();
      spawn.setWorld(Objects.requireNonNull(this.getWorld(), "World is null"));

      BlockFace gameDirection = ConfigFile.gameDirection;
      Vector vector = gameDirection.getDirection();
      Location entitySpawnPoint = spawn.add(vector.clone().multiply(length - levelLength));
      double randomMargin = 10 + RandomUtil.RANDOM.nextDouble(levelLength - 10);
      entitySpawnPoint.add(vector.multiply(randomMargin));

      entitySpawnPoint.getChunk().load();
      Entity entity = modifiedEntity.initiate(entitySpawnPoint);
      this.entitySpawnInterceptor.postEntitySpawn(entity, modifiedEntity);
    }
  }

  @Override
  public void clearEntities(Set<Integer> entityIds) {
    Objects.requireNonNull(Bukkit.getWorld(this.sessionContext.getWorldName()), "World is null")
      .getLivingEntities()
      .stream()
      .filter(livingEntity -> livingEntity instanceof Monster)
      .filter(livingEntity -> entityIds.contains(livingEntity.getEntityId()))
      .peek(livingEntity -> entityIds.remove(livingEntity.getEntityId()))
      .forEach(Entity::remove);
  }

  private void deleteWorld() {
    String worldName = this.sessionContext.getWorldName();
    if (this.getWorld() != null) {
      Bukkit.unloadWorld(worldName, false);
      this.session.getMapManager().deleteWorld();
    }
  }
}
