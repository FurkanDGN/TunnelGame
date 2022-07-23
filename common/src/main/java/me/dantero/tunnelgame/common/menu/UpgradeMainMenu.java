package me.dantero.tunnelgame.common.menu;

import com.gmail.furkanaxx34.dlibrary.bukkit.color.XColor;
import com.gmail.furkanaxx34.dlibrary.bukkit.menu.BaseMenu;
import com.gmail.furkanaxx34.dlibrary.replaceable.RpString;
import com.gmail.furkanaxx34.dlibrary.transformer.annotations.Exclude;
import com.gmail.furkanaxx34.dlibrary.transformer.annotations.Names;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * @author Furkan Doğan
 */
@Names(modifier = Names.Modifier.TO_LOWER_CASE, strategy = Names.Strategy.HYPHEN_CASE)
public class UpgradeMainMenu extends BaseMenu {

  public static RpString title = RpString.from("&8Progress of Jobs")
    .map(XColor::colorize);

  public static int row = 3;

  @Exclude
  @Nullable
  private static UpgradeMainMenu menu;

  private UpgradeMainMenu() {
  }

  public static void loadFile(@NotNull final Plugin plugin) {
    if (UpgradeMainMenu.menu == null) {
      UpgradeMainMenu.menu = new UpgradeMainMenu();
    }

    UpgradeMainMenu.menu.load(plugin);
  }

  public static void open(@NotNull Player player) {
    Objects.requireNonNull(menu, "initiate first!");
  }
}
