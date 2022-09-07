package me.dantero.tunnelgame.plugin.session.manager;

import me.dantero.tunnelgame.common.manager.WorldManager;
import me.dantero.tunnelgame.common.util.FilenameUtil;
import org.apache.commons.io.FileUtils;

import java.io.File;

/**
 * @author Furkan Doğan
 */
public class MapManager {

  private final File worldPath;
  private final File recoverPath;
  private final WorldManager worldManager;
  private final String worldName;

  public MapManager(File worldPath,
                    File recoverPath,
                    WorldManager worldManager) {
    this.worldPath = worldPath;
    this.recoverPath = recoverPath;
    this.worldManager = worldManager;
    this.worldName = FilenameUtil.withoutExtension(worldPath);
  }

  public String getWorldName() {
    return this.worldName;
  }

  public void deleteWorld() {
    try {
      this.worldManager.deleteWorld(this.worldName);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void cloneAndLoadWorld() {
    try {
      FileUtils.copyFile(this.recoverPath, this.worldPath);
      this.worldManager.loadWorld(this.worldName);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void reloadMap() throws Exception {
    this.worldManager.deleteWorld(this.worldName);
    this.cloneAndLoadWorld();
  }
}
