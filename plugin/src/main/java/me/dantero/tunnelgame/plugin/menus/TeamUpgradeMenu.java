package me.dantero.tunnelgame.plugin.menus;

import com.gmail.furkanaxx34.dlibrary.bukkit.bukkititembuilder.ItemStackBuilder;
import com.gmail.furkanaxx34.dlibrary.bukkit.color.XColor;
import com.gmail.furkanaxx34.dlibrary.bukkit.element.FileElement;
import com.gmail.furkanaxx34.dlibrary.bukkit.menu.BaseMenu;
import com.gmail.furkanaxx34.dlibrary.bukkit.smartinventory.InventoryContents;
import com.gmail.furkanaxx34.dlibrary.replaceable.RpString;
import com.gmail.furkanaxx34.dlibrary.transformer.annotations.Exclude;
import com.gmail.furkanaxx34.dlibrary.transformer.annotations.Names;
import com.gmail.furkanaxx34.dlibrary.xseries.XMaterial;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
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
public class TeamUpgradeMenu extends BaseMenu {

  public static RpString title = RpString.from("&8Upgrades")
    .map(XColor::colorize);

  public static int row = 3;

  @NotNull
  public static FileElement sharpness = FileElement.insert(
    ItemStackBuilder.from(XMaterial.NETHERITE_CHESTPLATE)
      .setName("&eSelf Upgrade")
      .getItemStack(),
    1,
    1
  );

  @NotNull
  public static FileElement protection = FileElement.insert(
    ItemStackBuilder.from(XMaterial.COOKED_BEEF)
      .setName("&eSelf Upgrade")
      .getItemStack(),
    1,
    1
  );

  @Nullable
  @Exclude
  private static TeamUpgradeMenu menu;

  public static void loadConfig(Plugin plugin) {
    if (TeamUpgradeMenu.menu == null) {
      TeamUpgradeMenu.menu = new TeamUpgradeMenu();
    }
    TeamUpgradeMenu.menu.load(plugin);
  }

  public static void open(Player player) {
    Objects.requireNonNull(menu, "initiate first!");
    menu.openPage(player, row, "team-upgrade-gui", title, initEvent -> {
      InventoryContents contents = initEvent.contents();

    });
  }
}
