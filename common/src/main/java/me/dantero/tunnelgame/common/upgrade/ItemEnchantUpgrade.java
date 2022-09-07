package me.dantero.tunnelgame.common.upgrade;

import me.dantero.tunnelgame.common.InventorySlot;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

/**
 * @author Furkan Doğan
 */
public class ItemEnchantUpgrade implements Applicable {

  private final Enchantment enchantment;
  private final int level;
  private final InventorySlot[] inventorySlots;

  public ItemEnchantUpgrade(Enchantment enchantment,
                            int level,
                            InventorySlot... inventorySlots) {
    this.enchantment = enchantment;
    this.level = level;
    this.inventorySlots = inventorySlots;
  }

  @Override
  public void apply(Player player) {
    PlayerInventory inventory = player.getInventory();
    for (InventorySlot inventorySlot : this.inventorySlots) {
      ItemStack item = inventory.getItem(inventorySlot.slot());
      if (item == null) return;

      item.addEnchantment(this.enchantment, this.level);
      inventory.setItem(inventorySlot.slot(), item);
      player.updateInventory();
    }
  }
}
