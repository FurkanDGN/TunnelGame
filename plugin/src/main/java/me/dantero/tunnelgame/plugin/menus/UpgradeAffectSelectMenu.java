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
import me.dantero.tunnelgame.common.game.SessionContext;
import me.dantero.tunnelgame.common.upgrade.AffectType;
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
public class UpgradeAffectSelectMenu extends BaseMenu {

  public static RpString title = RpString.from("&8Who do you want to upgrade?")
    .map(XColor::colorize);

  public static int row = 3;

  @NotNull
  public static FileElement selfUpgrade = FileElement.insert(
    ItemStackBuilder.from(XMaterial.NETHER_STAR)
      .setName("&eSelf Upgrade")
      .setLore("&eClick to make self upgrade")
      .getItemStack(),
    1,
    3
  );

  @NotNull
  public static FileElement teamUpgrade = FileElement.insert(
    ItemStackBuilder.from(XMaterial.ZOMBIE_HEAD)
      .setName("&eTeam Upgrade")
      .setLore("&eClick to make team upgrade")
      .getItemStack(),
    1,
    5
  );

  @Nullable
  @Exclude
  private static UpgradeAffectSelectMenu menu;

  public static void loadConfig(Plugin plugin) {
    if (UpgradeAffectSelectMenu.menu == null) {
      UpgradeAffectSelectMenu.menu = new UpgradeAffectSelectMenu();
    }
    UpgradeAffectSelectMenu.menu.load(plugin);
  }

  public static void open(Player player, SessionContext sessionContext) {
    Objects.requireNonNull(menu, "initiate first!");
    menu.openPage(player, row, "upgrade-affect-select-gui", title, initEvent -> {
      InventoryContents contents = initEvent.contents();
      selfUpgrade
        .addEvent(clickEvent -> SelfUpgradeMenu.open(player, sessionContext))
        .place(contents);
      teamUpgrade
        .addEvent(clickEvent -> TeamUpgradeMenu.open(player, sessionContext))
        .place(contents);
    });
  }
}
