package com.github.furkandgn.tunnelgame.common.util.game;

import java.io.File;

/**
 * @author Furkan Doğan
 */
public final class FilenameUtil {

  public static String withoutExtension(File file) {
    return file.getName().replaceFirst("[.][^.]+$", "");
  }
}
