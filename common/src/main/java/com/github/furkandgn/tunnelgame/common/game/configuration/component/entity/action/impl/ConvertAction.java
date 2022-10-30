package com.github.furkandgn.tunnelgame.common.game.configuration.component.entity.action.impl;

import com.github.furkandgn.tunnelgame.common.Constants;
import com.github.furkandgn.tunnelgame.common.game.configuration.ModifiedEntity;
import com.github.furkandgn.tunnelgame.common.util.game.RandomUtil;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

/**
 * @author Furkan Doğan
 */
public class ConvertAction extends AbstractAction {

  private final static Material CONVERT_ITEM = Material.OAK_WOOD;

  private int amount;

  @Override
  protected void init(ConfigurationSection configurationSection) {
    this.amount = configurationSection.getInt("amount");
  }

  @Override
  protected void onEntityAttackPlayer(ModifiedEntity modifiedEntity, EntityDamageByEntityEvent event) {
    Entity entity = event.getEntity();
    if (entity instanceof Player player) {
      PlayerInventory inventory = player.getInventory();
      for (int i = 0; i < this.amount; i++) {
        this.convertRandomItem(inventory);
        player.updateInventory();
      }
    }
  }

  @Override
  protected long getPeriod() {
    return Constants.DEFAULT_COOLDOWN;
  }

  private void convertRandomItem(PlayerInventory inventory) {
    int random = RandomUtil.randomIntInRange(9);
    ItemStack itemStack = new ItemStack(CONVERT_ITEM);
    inventory.setItem(random, itemStack);
  }
}
