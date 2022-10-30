package com.github.furkandgn.tunnelgame.plugin.session.manager;

import com.github.furkandgn.tunnelgame.common.manager.MapManager;
import com.github.furkandgn.tunnelgame.common.manager.WorldManager;
import com.github.furkandgn.tunnelgame.common.util.game.FileUtil;
import com.github.furkandgn.tunnelgame.common.util.game.FilenameUtil;

import java.io.File;

/**
 * @author Furkan Doğan
 */
public class DefaultMapManager implements MapManager {

  private final File worldPath;
  private final File recoverPath;
  private final WorldManager worldManager;
  private final String worldName;

  public DefaultMapManager(File worldPath,
                           File recoverPath,
                           WorldManager worldManager) {
    this.worldPath = worldPath;
    this.recoverPath = recoverPath;
    this.worldManager = worldManager;
    this.worldName = FilenameUtil.withoutExtension(worldPath);
  }

  @Override
  public String getWorldName() {
    return this.worldName;
  }

  @Override
  public void deleteWorld() {
    try {
      this.worldManager.deleteWorld(this.worldName);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void cloneAndLoadWorld() {
    try {
      FileUtil.copyFile(this.recoverPath, this.worldPath);
      this.worldManager.loadWorld(this.worldName);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void reloadMap() throws Exception {
    this.worldManager.deleteWorld(this.worldName);
    this.cloneAndLoadWorld();
  }
}
