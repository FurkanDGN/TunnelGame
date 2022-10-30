package com.github.furkandgn.tunnelgame.common.util.game;

import org.bukkit.enchantments.Enchantment;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * @author Furkan Doğan
 */
public final class EnchantUtil {

  private static final Map<String, Enchantment> mappedEnchantments = new HashMap<>() {{
    this.put("protection", Enchantment.PROTECTION_ENVIRONMENTAL);
  }};

  public static Enchantment getByName(String name) {
    return mappedEnchantments.getOrDefault(name.toLowerCase(Locale.ENGLISH), Enchantment.getByName(name));
  }
}
