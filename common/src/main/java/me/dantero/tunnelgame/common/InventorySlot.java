package me.dantero.tunnelgame.common;

import com.gmail.furkanaxx34.dlibrary.bukkit.utils.NumberUtil;
import org.bukkit.inventory.EquipmentSlot;

import java.util.Locale;

/**
 * @author Furkan Doğan
 */
public record InventorySlot(int slot) {

  private static final int HAND_SLOT = 36;
  private static final int OFF_HAND_SLOT = 45;
  private static final int HEAD_SLOT = 5;
  private static final int CHEST_SLOT = 6;
  private static final int LEGS_SLOT = 7;
  private static final int FEET_SLOT = 8;

  public static final InventorySlot HAND = new InventorySlot(HAND_SLOT);
  public static final InventorySlot OFF_HAND = new InventorySlot(OFF_HAND_SLOT);
  public static final InventorySlot HEAD = new InventorySlot(HEAD_SLOT);
  public static final InventorySlot CHEST = new InventorySlot(CHEST_SLOT);
  public static final InventorySlot LEGS = new InventorySlot(LEGS_SLOT);
  public static final InventorySlot FEET = new InventorySlot(FEET_SLOT);

  public static InventorySlot fromString(String str) {
    if (NumberUtil.isInteger(str)) {
      return new InventorySlot(Integer.parseInt(str));
    } else {
      return switch (str.toUpperCase(Locale.ENGLISH).trim()) {
        case "HAND" -> HAND;
        case "OFF_HAND", "OFFHAND" -> OFF_HAND;
        case "HEAD" -> HEAD;
        case "CHEST" -> CHEST;
        case "LEGS" -> LEGS;
        case "FEET" -> FEET;
        default -> throw new IllegalArgumentException("Inventory slot is not supported.");
      };
    }
  }

  public EquipmentSlot asEquipmentSlot() {
    return switch (this.slot) {
      case HAND_SLOT -> EquipmentSlot.HAND;
      case OFF_HAND_SLOT -> EquipmentSlot.OFF_HAND;
      case HEAD_SLOT -> EquipmentSlot.HEAD;
      case CHEST_SLOT -> EquipmentSlot.CHEST;
      case LEGS_SLOT -> EquipmentSlot.LEGS;
      case FEET_SLOT -> EquipmentSlot.FEET;
      default -> throw new IllegalArgumentException("Equipment is not supported");
    };
  }
}
