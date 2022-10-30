package com.github.furkandgn.tunnelgame.plugin.manager;

import com.github.furkandgn.tunnelgame.common.config.UpgradeConfigFile;
import com.github.furkandgn.tunnelgame.common.config.pojo.UpgradeConfig;
import com.github.furkandgn.tunnelgame.common.manager.InventoryManager;
import org.bukkit.entity.Player;

import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * @author Furkan Doğan
 */
public class DefaultInventoryManager implements InventoryManager {

  private final Supplier<Stream<Player>> supplier;

  public DefaultInventoryManager(Supplier<Stream<Player>> supplier) {
    this.supplier = supplier;
  }

  @Override
  public void giveStarterKit() {
    this.supplier.get()
      .peek(player -> this.applyUpgrade(player, UpgradeConfigFile.selfArmor.get(0)))
      .peek(player -> this.applyUpgrade(player, UpgradeConfigFile.selfFood.get(0)))
      .forEach(player -> this.applyUpgrade(player, UpgradeConfigFile.selfWeapon.get(0)));
  }

  private void applyUpgrade(Player player, UpgradeConfig upgradeConfig) {
    if (upgradeConfig == null) return;
    upgradeConfig.applicable().apply(player);
  }
}