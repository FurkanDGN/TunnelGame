package me.dantero.tunnelgame.common.util;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Arrays;

/**
 * @author Furkan Doğan
 */
public class FileUtil {

  public static void saveResources(final Plugin plugin, final @NotNull String... paths) {
    Arrays.stream(paths).forEach(path -> {
      if (!new File(plugin.getDataFolder(), path).exists()) {
        plugin.saveResource(path, false);
      }
    });
  }
}
