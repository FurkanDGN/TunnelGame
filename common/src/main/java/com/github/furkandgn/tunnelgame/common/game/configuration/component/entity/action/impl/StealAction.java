package com.github.furkandgn.tunnelgame.common.game.configuration.component.entity.action.impl;

import com.github.furkandgn.tunnelgame.common.Constants;
import com.github.furkandgn.tunnelgame.common.game.configuration.ModifiedEntity;
import com.github.furkandgn.tunnelgame.common.util.game.RandomUtil;
import org.bukkit.Bukkit;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author Furkan Doğan
 */
public class StealAction extends AbstractAction {

  private int amount;

  @Override
  protected void init(ConfigurationSection configurationSection) {
    this.amount = configurationSection.getInt("amount");
  }

  @Override
  public void onPlayerMove(ModifiedEntity modifiedEntity, PlayerMoveEvent playerMoveEvent) {
    Entity entity = modifiedEntity.entity();
    Player player = playerMoveEvent.getPlayer();
    PlayerInventory inventory = player.getInventory();

    for (int i = 0; i < this.amount; i++) {
      ItemStack item = this.selectRandomItem(inventory);
      if (item == null) {
        continue;
      }
      inventory.removeItem(item);
      if (entity instanceof Enderman enderman) {
        try {
          BlockData blockData = Bukkit.createBlockData(item.getType());
          enderman.setCarriedBlock(blockData);
        } catch (Exception ignored) {
        }
      }
    }

    player.updateInventory();
  }

  @Override
  protected long getPeriod() {
    return Constants.DEFAULT_COOLDOWN;
  }

  private ItemStack selectRandomItem(PlayerInventory inventory) {
    List<ItemStack> itemStacks = Arrays.stream(inventory.getContents())
      .filter(Objects::nonNull)
      .toList();
    int size = itemStacks.size();
    if (size == 0) return null;

    int random = RandomUtil.randomIntInRange(size);

    return itemStacks.get(random);
  }
}
