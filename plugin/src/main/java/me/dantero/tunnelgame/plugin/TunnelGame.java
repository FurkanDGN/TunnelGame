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

public final class TunnelGame extends JavaPlugin {

  private SessionManager sessionManager;

  @Override
  public void onEnable() {
    DLibrary.initialize(this);
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

    SessionManager sessionManager = new DefaultSessionManager();
    PointManager pointManager = new DefaultPointManager();
    JoinHandler joinHandler = new DefaultJoinHandler(sessionManager);

    SlimePlugin slimePlugin = (SlimePlugin) Bukkit.getPluginManager().getPlugin("SlimeWorldManager");
    WorldManager worldManager = new SlimeWorldManager(slimePlugin);

    File file = new File(this.getDataFolder() + File.separator + "worlds", "default.slime");
    Session session = new WorldSession(file, worldManager, LevelConfigFile.levelConfiguration);
    sessionManager.setupSession(session);
    session.prepare();

    this.registerEvents(sessionManager, pointManager, joinHandler);
  }

  @Override
  public void onDisable() {
    Optional.ofNullable(this.sessionManager)
      .ifPresent(sessionManager1 -> sessionManager1.sessions().forEach(Session::stop));
  }

  private void registerEvents(SessionManager sessionManager,
                              PointManager pointManager,
                              JoinHandler joinHandler) {
    new BasicListeners(this, sessionManager, pointManager, joinHandler)
      .register();
    new PlayerMoveListener(this, sessionManager)
      .register();
    new ModifiedEntityListeners(this, sessionManager)
      .register();
  }
}
