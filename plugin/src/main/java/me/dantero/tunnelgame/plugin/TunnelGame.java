package me.dantero.tunnelgame.plugin;

import com.gmail.furkanaxx34.dlibrary.bukkit.DLibrary;
import com.grinderwolf.swm.api.SlimePlugin;
import me.dantero.tunnelgame.common.config.ConfigFile;
import me.dantero.tunnelgame.common.config.LanguageFile;
import me.dantero.tunnelgame.common.config.LevelConfigFile;
import me.dantero.tunnelgame.common.config.UpgradeConfigFile;
import me.dantero.tunnelgame.common.game.Session;
import me.dantero.tunnelgame.common.handlers.JoinHandler;
import me.dantero.tunnelgame.common.manager.PointManager;
import me.dantero.tunnelgame.common.manager.SessionManager;
import me.dantero.tunnelgame.common.manager.WorldManager;
import me.dantero.tunnelgame.common.util.FileUtil;
import me.dantero.tunnelgame.plugin.handler.DefaultJoinHandler;
import me.dantero.tunnelgame.plugin.listeners.BasicListeners;
import me.dantero.tunnelgame.plugin.listeners.LobbyListeners;
import me.dantero.tunnelgame.plugin.listeners.ModifiedEntityListeners;
import me.dantero.tunnelgame.plugin.listeners.PlayerMoveListener;
import me.dantero.tunnelgame.plugin.manager.DefaultPointManager;
import me.dantero.tunnelgame.plugin.manager.DefaultSessionManager;
import me.dantero.tunnelgame.plugin.manager.SlimeWorldManager;
import me.dantero.tunnelgame.plugin.menus.CompleteUpgradeMenu;
import me.dantero.tunnelgame.plugin.menus.SelfUpgradeMenu;
import me.dantero.tunnelgame.plugin.menus.TeamUpgradeMenu;
import me.dantero.tunnelgame.plugin.menus.UpgradeAffectSelectMenu;
import me.dantero.tunnelgame.plugin.session.WorldSession;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Optional;

@SuppressWarnings("unused")
public final class TunnelGame extends JavaPlugin {

  private SessionManager sessionManager;

  @Override
  public void onEnable() {
    this.initialize();
  }

  @Override
  public void onDisable() {
    Optional.ofNullable(this.sessionManager)
      .ifPresent(manager -> manager.sessions().forEach(Session::stop));
  }

  private void initialize() {
    DLibrary.initialize(this);
    this.loadFiles();
    this.sessionManager = new DefaultSessionManager();
    PointManager pointManager = new DefaultPointManager();
    JoinHandler joinHandler = new DefaultJoinHandler(this.sessionManager);
    if (!ConfigFile.lobbyMode) this.initializeSessions();
    this.registerEvents(this.sessionManager, pointManager, joinHandler);
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
  }

  private void initializeSessions() {
    SlimePlugin slimePlugin = (SlimePlugin) Bukkit.getPluginManager().getPlugin("SlimeWorldManager");
    WorldManager worldManager = new SlimeWorldManager(slimePlugin);
    File file = new File(this.getDataFolder() + File.separator + "worlds", "default.slime");

    for (int i = 0; i < ConfigFile.maxSessionCount; i++) {
      this.initNewSession(worldManager, file);
    }
  }

  private void initNewSession(WorldManager worldManager, File file) {
    Session session = new WorldSession(file, worldManager, LevelConfigFile.levelConfiguration, this);
    this.sessionManager.setupSession(session);
  }

  private void registerEvents(SessionManager sessionManager,
                              PointManager pointManager,
                              JoinHandler joinHandler) {
    if (ConfigFile.lobbyMode) {
      Bukkit.getPluginManager().registerEvents(new LobbyListeners(), this);
    } else {
      new BasicListeners(this, sessionManager, pointManager, joinHandler)
        .register();
      new PlayerMoveListener(this, sessionManager)
        .register();
      new ModifiedEntityListeners(this, sessionManager)
        .register();
    }
  }
}
