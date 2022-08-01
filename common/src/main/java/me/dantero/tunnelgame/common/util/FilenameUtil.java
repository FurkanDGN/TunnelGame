package me.dantero.tunnelgame.common.util;

import java.io.File;

/**
 * @author Furkan Doğan
 */
public final class FilenameUtil {

  public static String withoutExtension(File file) {
    return file.getName().replaceFirst("[.][^.]+$", "");
  }
}
