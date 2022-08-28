package me.dantero.tunnelgame.plugin.menus;

import com.gmail.furkanaxx34.dlibrary.bukkit.color.XColor;
import com.gmail.furkanaxx34.dlibrary.bukkit.menu.BaseMenu;
import com.gmail.furkanaxx34.dlibrary.bukkit.smartinventory.InventoryContents;
import com.gmail.furkanaxx34.dlibrary.replaceable.RpString;
import com.gmail.furkanaxx34.dlibrary.transformer.annotations.Exclude;
import com.gmail.furkanaxx34.dlibrary.transformer.annotations.Names;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import me.dantero.tunnelgame.common.config.pojo.UpgradeConfig;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * @author Furkan Doğan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Names(modifier = Names.Modifier.TO_LOWER_CASE, strategy = Names.Strategy.HYPHEN_CASE)
public class CompleteUpgradeMenu extends BaseMenu {

  public static RpString title = RpString.from("&8Select")
    .map(XColor::colorize);

  public static int row = 6;

  @Nullable
  @Exclude
  private static CompleteUpgradeMenu menu;

  public static void loadConfig(Plugin plugin) {
    if (CompleteUpgradeMenu.menu == null) {
      CompleteUpgradeMenu.menu = new CompleteUpgradeMenu();
    }

    CompleteUpgradeMenu.menu.load(plugin);
  }

  public static void open(Player player, int currentLevel, Map<Integer, UpgradeConfig> configMap, Consumer<Player> consumer) {
    Objects.requireNonNull(menu, "initiate first!");
    menu.openPage(player, row, "complete-upgrade-gui", title, initEvent -> {
      InventoryContents contents = initEvent.contents();

    });
  }
}
