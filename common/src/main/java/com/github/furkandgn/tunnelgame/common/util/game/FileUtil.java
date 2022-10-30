package com.github.furkandgn.tunnelgame.common.util.game;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Objects;

/**
 * @author Furkan Doğan
 */
@SuppressWarnings({"SameParameterValue", "UnusedReturnValue"})
public class FileUtil {

  public static void saveResources(final Plugin plugin, final @NotNull String... paths) {
    Arrays.stream(paths).forEach(path -> {
      if (!new File(plugin.getDataFolder(), path).exists()) {
        plugin.saveResource(path, false);
      }
    });
  }

  public static void copyFile(File srcFile, File destFile) throws IOException {
    copyFile(srcFile, destFile, StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
  }

  public static void copyFile(File srcFile, File destFile, CopyOption... copyOptions) throws IOException {
    requireFileCopy(srcFile, destFile);
    requireFile(srcFile, "srcFile");
    requireCanonicalPathsNotEquals(srcFile, destFile);
    createParentDirectories(destFile);
    requireFileIfExists(destFile, "destFile");
    if (destFile.exists()) {
      requireCanWrite(destFile, "destFile");
    }

    Files.copy(srcFile.toPath(), destFile.toPath(), copyOptions);
    requireEqualSizes(srcFile, destFile, srcFile.length(), destFile.length());
  }

  private static void requireFileCopy(File source, File destination) throws FileNotFoundException {
    requireExistsChecked(source, "source");
    Objects.requireNonNull(destination, "destination");
  }

  private static File requireExistsChecked(File file, String fileParamName) throws FileNotFoundException {
    Objects.requireNonNull(file, fileParamName);
    if (!file.exists()) {
      throw new FileNotFoundException("File system element for parameter '" + fileParamName + "' does not exist: '" + file + "'");
    } else {
      return file;
    }
  }

  private static File requireFile(File file, String name) {
    Objects.requireNonNull(file, name);
    if (!file.isFile()) {
      throw new IllegalArgumentException("Parameter '" + name + "' is not a file: " + file);
    } else {
      return file;
    }
  }

  private static File requireFileIfExists(File file, String name) {
    Objects.requireNonNull(file, name);
    return file.exists() ? requireFile(file, name) : file;
  }

  private static void requireCanonicalPathsNotEquals(File file1, File file2) throws IOException {
    String canonicalPath = file1.getCanonicalPath();
    if (canonicalPath.equals(file2.getCanonicalPath())) {
      throw new IllegalArgumentException(String.format("File canonical paths are equal: '%s' (file1='%s', file2='%s')", canonicalPath, file1, file2));
    }
  }

  public static File createParentDirectories(File file) throws IOException {
    return mkdirs(getParentFile(file));
  }

  private static File mkdirs(File directory) throws IOException {
    if (directory != null && !directory.mkdirs() && !directory.isDirectory()) {
      throw new IOException("Cannot create directory '" + directory + "'.");
    } else {
      return directory;
    }
  }

  private static File getParentFile(File file) {
    return file == null ? null : file.getParentFile();
  }

  private static void requireCanWrite(File file, String name) {
    Objects.requireNonNull(file, "file");
    if (!file.canWrite()) {
      throw new IllegalArgumentException("File parameter '" + name + " is not writable: '" + file + "'");
    }
  }

  private static void requireEqualSizes(File srcFile, File destFile, long srcLen, long dstLen) throws IOException {
    if (srcLen != dstLen) {
      throw new IOException("Failed to copy full contents from '" + srcFile + "' to '" + destFile + "' Expected length: " + srcLen + " Actual: " + dstLen);
    }
  }
}
