package me.dantero.tunnelgame.plugin.session.manager;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Furkan Doğan
 */
public final class PlayerInventoryStore {

  private final Map<UUID, byte[]> inventoryMap = new HashMap<>();
  private final Map<UUID, Location> locationMap  = new HashMap<>();

  public void savePlayer(Player player) {
    UUID uniqueId = player.getUniqueId();
    Location location = player.getLocation();
    PlayerInventory inventory = player.getInventory();
    byte[] inventoryToByteArray = this.toByteArray(inventory);
    this.inventoryMap.put(uniqueId, inventoryToByteArray);
    this.locationMap.put(uniqueId, location);
  }

  public void resetPlayer(Player player) {
    try {
      UUID uniqueId = player.getUniqueId();
      PlayerInventory playerInventory = player.getInventory();
      try {
        if (!this.inventoryMap.containsKey(uniqueId)) return;
        byte[] bytes = this.inventoryMap.get(uniqueId);
        Inventory inventory = this.fromByteArray(bytes);
        for (int i = 0; i < inventory.getSize(); i++) {
          ItemStack item = inventory.getItem(i);
          playerInventory.setItem(i, item);
        }
      } catch (IOException e) {
        e.printStackTrace();
      } finally {
        if (this.locationMap.containsKey(uniqueId)) {
          Location oldLocation = this.locationMap.get(uniqueId);
          player.teleport(oldLocation);
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private byte[] toByteArray(Inventory inventory) {
    try {
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

      // Write the size of the inventory
      dataOutput.writeInt(inventory.getSize());

      // Save every element in the list
      for (int i = 0; i < inventory.getSize(); i++) {
        dataOutput.writeObject(inventory.getItem(i));
      }

      // Serialize that array
      dataOutput.close();
      return outputStream.toByteArray();
    } catch (Exception e) {
      throw new IllegalStateException("Unable to save item stacks.", e);
    }
  }

  private Inventory fromByteArray(byte[] data) throws IOException {
    try {
      ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
      BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
      Inventory inventory = Bukkit.getServer().createInventory(null, dataInput.readInt());

      // Read the serialized inventory
      for (int i = 0; i < inventory.getSize(); i++) {
        inventory.setItem(i, (ItemStack) dataInput.readObject());
      }
      dataInput.close();
      return inventory;
    } catch (ClassNotFoundException e) {
      throw new IOException("Unable to decode class type.", e);
    }
  }
}
