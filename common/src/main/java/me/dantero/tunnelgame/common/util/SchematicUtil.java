package me.dantero.tunnelgame.common.util;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Objects;

/**
 * @author Furkan Doğan
 */
public final class SchematicUtil {

  @Nullable
  public static ClipboardHolder load(@NotNull File file) {
    final ClipboardFormat format = ClipboardFormats.findByFile(file);
    if (format == null) {
      return null;
    }

    try (FileInputStream fileInputStream = new FileInputStream(file)) {
      byte[] bytes = fileInputStream.readAllBytes();
      ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
      final ClipboardReader reader = format.getReader(byteArrayInputStream);
      Clipboard read = reader.read();
      return new ClipboardHolder(read);
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  public static void pasteSchematic(@NotNull ClipboardHolder clipboardHolder, @NotNull Location location) {
    Objects.requireNonNull(location, "location is null");
    Objects.requireNonNull(location.getWorld(), "world is null");

    World world = location.getWorld();

    try (final EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(world))) {
      final BlockVector3 blockVector3 = BlockVector3.at(
        location.getX(),
        location.getY(),
        location.getZ()
      );

      final Operation operation = clipboardHolder
        .createPaste(editSession)
        .to(blockVector3)
        .ignoreAirBlocks(true)
        .copyBiomes(false)
        .copyEntities(false)
        .build();

      Operations.complete(operation);
    } catch (final WorldEditException exception) {
      exception.printStackTrace();
    }
  }
}
