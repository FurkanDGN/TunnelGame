package me.dantero.tunnelgame.common.game.configuration.component;

import me.dantero.tunnelgame.common.util.EnchantUtil;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public final class EquipmentComponent {

  private final Map<EquipmentSlot, ItemStack> equipment = new HashMap<>();

  public EquipmentComponent(final ConfigurationSection configurationSection) {
    if (configurationSection == null) {
      return;
    }

    for (final String key : configurationSection.getKeys(false)) {
      final ConfigurationSection itemSection = configurationSection.getConfigurationSection(key);
      if (itemSection == null) {
        continue;
      }

      final ItemStack item = new ItemStack(Material.valueOf(key));
      for (final String enchantmentRaw : itemSection.getStringList("enchantment")) {
        final String enchantmentType = enchantmentRaw.split(":")[0];
        Enchantment enchantment = EnchantUtil.getByName(enchantmentType);
        final int level = Integer.parseInt(enchantmentRaw.split(":")[1]);

        item.addEnchantment(enchantment, level);
      }

      this.equipment.put(EquipmentSlot.valueOf(itemSection.getString("slot")), item);
    }
  }

  public void apply(final Entity entity) {
    this.equipment.forEach((equipmentSlot, itemStack) -> {
      if (entity instanceof final Player player)
        player.getInventory().setItem(equipmentSlot, itemStack);
      else if (entity instanceof final LivingEntity livingEntity) {
        final EntityEquipment equipment = livingEntity.getEquipment();
        if (equipment == null) return;

        equipment.setItem(equipmentSlot, itemStack);
      }
    });
  }
}