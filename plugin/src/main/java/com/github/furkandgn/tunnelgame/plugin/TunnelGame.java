package com.github.furkandgn.tunnelgame.plugin;

import com.github.furkandgn.tunnelgame.common.config.*;
import com.github.furkandgn.tunnelgame.common.proto.JoinRequest;
import com.gmail.furkanaxx34.dlibrary.bukkit.DLibrary;
import com.grinderwolf.swm.api.SlimePlugin;
import com.github.furkandgn.tunnelgame.common.game.Session;
import com.github.furkandgn.tunnelgame.common.handlers.JoinHandler;
import com.github.furkandgn.tunnelgame.common.manager.PointManager;
import com.github.furkandgn.tunnelgame.common.manager.SessionManager;
import com.github.furkandgn.tunnelgame.common.manager.SignManager;
import com.github.furkandgn.tunnelgame.common.manager.WorldManager;
import com.github.furkandgn.tunnelgame.common.misc.ServerInfos;
import com.github.furkandgn.tunnelgame.common.misc.Servers;
import com.github.furkandgn.tunnelgame.common.misc.Topics;
import com.github.furkandgn.tunnelgame.common.redis.PubSub;
import com.github.furkandgn.tunnelgame.common.redis.Redis;
import com.github.furkandgn.tunnelgame.common.util.game.BungeeUtil;
import com.github.furkandgn.tunnelgame.common.util.game.FileUtil;
import com.github.furkandgn.tunnelgame.plugin.handler.DefaultJoinHandler;
import com.github.furkandgn.tunnelgame.plugin.listeners.BasicListeners;
import com.github.furkandgn.tunnelgame.plugin.listeners.LobbyListeners;
import com.github.furkandgn.tunnelgame.plugin.listeners.ModifiedEntityListeners;
import com.github.furkandgn.tunnelgame.plugin.listeners.PlayerMoveListener;
import com.github.furkandgn.tunnelgame.plugin.manager.DefaultPointManager;
import com.github.furkandgn.tunnelgame.plugin.manager.DefaultSessionManager;
import com.github.furkandgn.tunnelgame.plugin.manager.GameSignManager;
import com.github.furkandgn.tunnelgame.plugin.manager.SlimeWorldManager;
import com.github.furkandgn.tunnelgame.plugin.menus.CompleteUpgradeMenu;
import com.github.furkandgn.tunnelgame.plugin.menus.SelfUpgradeMenu;
import com.github.furkandgn.tunnelgame.plugin.menus.TeamUpgradeMenu;
import com.github.furkandgn.tunnelgame.plugin.menus.UpgradeAffectSelectMenu;
import com.github.furkandgn.tunnelgame.plugin.session.WorldSession;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Optional;

@SuppressWarnings("unused")
public final class TunnelGame extends JavaPlugin {

  private SessionManager sessionManager;
  private PubSub pubSub;

  @Override
  public void onEnable() {
    this.initialize();
  }

  @Override
  public void onDisable() {
    Optional.ofNullable(this.sessionManager)
      .ifPresent(manager -> manager.sessions().forEach(Session::shutdown));
    Redis.get().close();
  }

  private void initialize() {
    DLibrary.initialize(this);
    this.loadFiles();
    this.sessionManager = new DefaultSessionManager();
    this.initiateSystems(this.sessionManager);
    PointManager pointManager = new DefaultPointManager();
    JoinHandler joinHandler = new DefaultJoinHandler(this.sessionManager);
    if (!ConfigFile.lobbyMode) this.initializeSessions(pointManager);
    SignManager signManager = new GameSignManager();
    this.registerEvents(this.sessionManager, pointManager, joinHandler, signManager);
  }

  private void loadFiles() {
    FileUtil.saveResources(this,
      "worlds/default.slime",
      "config.yml",
      "upgrade-config.yml",
      "level-config.yml");
    ConfigFile.loadFile(this);
    LanguageFile.loadFile(this);
    UpgradeConfigFile.loadFile(this);
    LevelConfigFile.loadFile(this);
    UpgradeAffectSelectMenu.loadConfig(this);
    SelfUpgradeMenu.loadConfig(this);
    TeamUpgradeMenu.loadConfig(this);
    CompleteUpgradeMenu.loadConfig(this);
    SignsFile.loadFile(this);
  }

  private void initiateSystems(SessionManager sessionManager) {
    Servers.init();
    Redis.init();
    if (ConfigFile.lobbyMode) {
      ServerInfos.initLobby();
    } else {
      Runnable runnable = ServerInfos.init(() -> -1, sessionManager::sessions);
      Bukkit.getScheduler().runTaskTimerAsynchronously(this, runnable, 1, 20);
    }
    this.pubSub = new PubSub(Topics.JOIN_PLAYER);
    BungeeUtil.init(this, this.pubSub);
    Bukkit.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
  }

  private void initializeSessions(PointManager pointManager) {
    SlimePlugin slimePlugin = (SlimePlugin) Bukkit.getPluginManager().getPlugin("SlimeWorldManager");
    WorldManager worldManager = new SlimeWorldManager(slimePlugin);
    File file = new File(this.getDataFolder() + File.separator + "worlds", "default.slime");

    for (int i = 0; i < ConfigFile.maxSessionCount; i++) {
      this.initNewSession(worldManager, file, pointManager);
    }
  }

  private void initNewSession(WorldManager worldManager, File file, PointManager pointManager) {
    Session session = new WorldSession(file, worldManager, LevelConfigFile.levelConfiguration, this, pointManager);
    this.sessionManager.setupSession(session);
  }

  private void registerEvents(SessionManager sessionManager,
                              PointManager pointManager,
                              JoinHandler joinHandler,
                              SignManager signManager) {
    if (ConfigFile.lobbyMode) {
      signManager.init();
      Bukkit.getPluginManager().registerEvents(new LobbyListeners(signManager), this);
    } else {
      new BasicListeners(this, sessionManager, pointManager, joinHandler)
        .register();
      new PlayerMoveListener(this, sessionManager)
        .register();
      new ModifiedEntityListeners(this, sessionManager)
        .register();

      this.pubSub.subscribe(JoinRequest.getDefaultInstance(), joinHandler::handleRequest);
    }
  }
}
