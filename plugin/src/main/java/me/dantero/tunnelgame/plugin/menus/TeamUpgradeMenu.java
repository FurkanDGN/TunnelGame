package me.dantero.tunnelgame.plugin.menus;

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
import me.dantero.tunnelgame.common.config.UpgradeConfigFile;
import me.dantero.tunnelgame.common.game.SessionContext;
import me.dantero.tunnelgame.common.upgrade.AffectType;
import me.dantero.tunnelgame.common.upgrade.UpgradeType;
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
    ItemStackBuilder.from(XMaterial.NETHERITE_SWORD)
      .setName("&eSharpness Upgrade")
      .getItemStack(),
    1,
    3
  );

  @NotNull
  public static FileElement protection = FileElement.insert(
    ItemStackBuilder.from(XMaterial.NETHERITE_CHESTPLATE)
      .setName("&eProtection Upgrade")
      .getItemStack(),
    1,
    5
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
  private static TeamUpgradeMenu menu;

  public static void loadConfig(Plugin plugin) {
    if (TeamUpgradeMenu.menu == null) {
      TeamUpgradeMenu.menu = new TeamUpgradeMenu();
    }
    TeamUpgradeMenu.menu.load(plugin);
  }

  public static void open(Player player, SessionContext sessionContext) {
    Objects.requireNonNull(menu, "initiate first!");

    menu.openPage(player, row, "team-upgrade-gui", title, initEvent -> {
      InventoryContents contents = initEvent.contents();

      backButton
        .addEvent(clickEvent -> UpgradeAffectSelectMenu.open(player, sessionContext))
        .place(contents);

      sharpness
        .addEvent(clickEvent -> CompleteUpgradeMenu.open(player, sessionContext, AffectType.TEAM, UpgradeType.SHARPNESS, UpgradeConfigFile.teamSharpness))
        .place(contents);

      protection
        .addEvent(clickEvent -> CompleteUpgradeMenu.open(player, sessionContext, AffectType.TEAM, UpgradeType.PROTECTION, UpgradeConfigFile.teamProtection))
        .place(contents);
    });
  }
}
