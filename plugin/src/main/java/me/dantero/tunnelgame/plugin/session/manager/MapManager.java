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

  public void reloadMap() {
    try {
      this.worldManager.deleteWorld(this.worldName);
      FileUtils.forceDelete(this.worldPath);
      if (this.recoverPath.isDirectory()) {
        FileUtils.copyDirectory(this.recoverPath, this.worldPath);
      } else {
        FileUtils.copyFile(this.recoverPath, this.worldPath);
      }
      this.worldManager.loadWorld(this.worldName);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
