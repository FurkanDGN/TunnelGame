package com.github.furkandgn.tunnelgame.plugin.menus;

import com.gmail.furkanaxx34.dlibrary.bukkit.bukkititembuilder.ItemStackBuilder;
import com.gmail.furkanaxx34.dlibrary.bukkit.color.XColor;
import com.gmail.furkanaxx34.dlibrary.bukkit.element.FileElement;
import com.gmail.furkanaxx34.dlibrary.bukkit.menu.BaseMenu;
import com.gmail.furkanaxx34.dlibrary.bukkit.smartinventory.InventoryContents;
import com.gmail.furkanaxx34.dlibrary.bukkit.utils.TaskUtilities;
import com.gmail.furkanaxx34.dlibrary.replaceable.RpString;
import com.gmail.furkanaxx34.dlibrary.transformer.annotations.Exclude;
import com.gmail.furkanaxx34.dlibrary.transformer.annotations.Names;
import com.gmail.furkanaxx34.dlibrary.xseries.XMaterial;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import com.github.furkandgn.tunnelgame.common.config.UpgradeConfigFile;
import com.github.furkandgn.tunnelgame.common.game.SessionContext;
import com.github.furkandgn.tunnelgame.common.upgrade.AffectType;
import com.github.furkandgn.tunnelgame.common.upgrade.UpgradeType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * @author Furkan Doğan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Names(modifier = Names.Modifier.TO_LOWER_CASE, strategy = Names.Strategy.HYPHEN_CASE)
public class SelfUpgradeMenu extends BaseMenu {

  public static RpString title = RpString.from("&8Upgrades")
    .map(XColor::colorize);

  public static int row = 3;

  @NotNull
  public static FileElement armor = FileElement.insert(
    ItemStackBuilder.from(XMaterial.NETHERITE_CHESTPLATE)
      .setName("&eArmor Upgrade")
      .getItemStack(),
    1,
    2
  );

  @NotNull
  public static FileElement food = FileElement.insert(
    ItemStackBuilder.from(XMaterial.COOKED_BEEF)
      .setName("&eFood Upgrade")
      .getItemStack(),
    1,
    4
  );

  @NotNull
  public static FileElement weapon = FileElement.insert(
    ItemStackBuilder.from(XMaterial.NETHERITE_SWORD)
      .setName("&eWeapon Upgrade")
      .getItemStack(),
    1,
    6
  );

  @NotNull
  public static FileElement backButton = FileElement.insert(
    ItemStackBuilder.from(XMaterial.PLAYER_HEAD)
      .asSkull()
      .setOwner("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzYyNTkwMmIzODllZDZjMTQ3NTc0ZTQyMmRhOGY4ZjM2MWM4ZWI1N2U3NjMxNjc2YTcyNzc3ZTdiMWQifX19")
      .setName("&eBack")
      .setLore("&7Click to go previous menu")
      .getItemStack(),
    2,
    0
  );

  @Nullable
  @Exclude
  private static SelfUpgradeMenu menu;

  public static void loadConfig(Plugin plugin) {
    if (SelfUpgradeMenu.menu == null) {
      SelfUpgradeMenu.menu = new SelfUpgradeMenu();
    }
    SelfUpgradeMenu.menu.load(plugin);
  }

  public static void open(Player player, SessionContext sessionContext) {
    Objects.requireNonNull(menu, "initiate first!");
    menu.openPage(player, row, "self-upgrade-gui", title, initEvent -> {
      InventoryContents contents = initEvent.contents();

      backButton
        .addEvent(clickEvent -> UpgradeAffectSelectMenu.open(player, sessionContext))
        .place(contents);

      armor
        .addEvent(clickEvent -> CompleteUpgradeMenu.open(player, sessionContext, AffectType.SELF, UpgradeType.ARMOR, UpgradeConfigFile.selfArmor))
        .place(contents);

      food
        .addEvent(clickEvent -> CompleteUpgradeMenu.open(player, sessionContext, AffectType.SELF, UpgradeType.FOOD, UpgradeConfigFile.selfFood))
        .place(contents);

      weapon
        .addEvent(clickEvent -> CompleteUpgradeMenu.open(player, sessionContext, AffectType.SELF, UpgradeType.WEAPON, UpgradeConfigFile.selfWeapon))
        .place(contents);
    });
  }
}
