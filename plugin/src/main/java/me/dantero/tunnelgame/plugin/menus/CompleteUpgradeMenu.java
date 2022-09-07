package me.dantero.tunnelgame.plugin.menus;

import com.gmail.furkanaxx34.dlibrary.bukkit.bukkititembuilder.ItemStackBuilder;
import com.gmail.furkanaxx34.dlibrary.bukkit.color.XColor;
import com.gmail.furkanaxx34.dlibrary.bukkit.element.FileElement;
import com.gmail.furkanaxx34.dlibrary.bukkit.menu.BaseMenu;
import com.gmail.furkanaxx34.dlibrary.bukkit.smartinventory.*;
import com.gmail.furkanaxx34.dlibrary.bukkit.utils.TaskUtilities;
import com.gmail.furkanaxx34.dlibrary.element.Placeholder;
import com.gmail.furkanaxx34.dlibrary.replaceable.RpString;
import com.gmail.furkanaxx34.dlibrary.transformer.annotations.Exclude;
import com.gmail.furkanaxx34.dlibrary.transformer.annotations.Names;
import com.gmail.furkanaxx34.dlibrary.xseries.XMaterial;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import me.dantero.tunnelgame.common.config.LanguageFile;
import me.dantero.tunnelgame.common.config.pojo.UpgradeConfig;
import me.dantero.tunnelgame.common.game.SessionContext;
import me.dantero.tunnelgame.common.manager.PointManager;
import me.dantero.tunnelgame.common.upgrade.AffectType;
import me.dantero.tunnelgame.common.upgrade.Applicable;
import me.dantero.tunnelgame.common.upgrade.UpgradeType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * @author Furkan Doğan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Names(modifier = Names.Modifier.TO_LOWER_CASE, strategy = Names.Strategy.HYPHEN_CASE)
public class CompleteUpgradeMenu extends BaseMenu {

  public static RpString title = RpString.from("&8Select")
    .map(XColor::colorize);

  public static int row = 6;

  public static ItemStack icon = ItemStackBuilder.from(XMaterial.STONE)
    .setName("&6&lLevel %level%")
    .setLore(
      "&7Required points: &e%required-points%",
      "&a",
      "&eClick to upgrade"
    )
    .getItemStack();

  @NotNull
  public static ItemStack next = ItemStackBuilder.from(XMaterial.REDSTONE_TORCH)
    .setName("&eNext page")
    .setLore("&7Click to go next page.")
    .getItemStack();

  @NotNull
  public static ItemStack previous = ItemStackBuilder.from(XMaterial.LEVER)
    .setName("&ePrevious page")
    .setLore("&7Click to go previous page.")
    .getItemStack();

  @NotNull
  public static FileElement backButton = FileElement.insert(
    ItemStackBuilder.from(XMaterial.PLAYER_HEAD)
      .asSkull()
      .setOwner("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzYyNTkwMmIzODllZDZjMTQ3NTc0ZTQyMmRhOGY4ZjM2MWM4ZWI1N2U3NjMxNjc2YTcyNzc3ZTdiMWQifX19")
      .setName("&eBack")
      .setLore("&7Click to go previous menu")
      .getItemStack(),
    5,
    0
  );

  @Nullable
  @Exclude
  private static CompleteUpgradeMenu menu;

  public static void loadConfig(Plugin plugin) {
    if (CompleteUpgradeMenu.menu == null) {
      CompleteUpgradeMenu.menu = new CompleteUpgradeMenu();
    }

    CompleteUpgradeMenu.menu.load(plugin);
  }

  public static void open(Player player, SessionContext sessionContext, AffectType affectType,
                          UpgradeType upgradeType, Map<Integer, UpgradeConfig> configMap) {
    Objects.requireNonNull(menu, "initiate first!");

    InventoryProvider provider = new InventoryProvider() {
      @Override
      public void init(@NotNull InventoryContents contents) {
        render(player, sessionContext, affectType, upgradeType, configMap, contents);
      }

      @Override
      public void update(@NotNull InventoryContents contents) {
        render(player, sessionContext, affectType, upgradeType, configMap, contents);
      }
    };

    menu.openPage(player, row, "complete-upgrade-gui", title, provider);
  }

  private static void render(Player player,
                             SessionContext sessionContext,
                             AffectType affectType,
                             UpgradeType upgradeType,
                             Map<Integer, UpgradeConfig> configMap,
                             InventoryContents contents) {
    UUID uniqueId = player.getUniqueId();

    Pagination pagination = contents.pagination();
    Icon[] icons = new Icon[configMap.size()];
    int pageItems = 9 * 5;
    pagination.setIconsPerPage(pageItems);

    PointManager pointManager = sessionContext.getPointManager();

    configMap.forEach((level, upgradeConfig) -> {
      if (level <= 0) throw new IllegalArgumentException("Level cannot be less or equal to 0");

      int requiredPoints = upgradeConfig.requiredPoints();
      XMaterial material = upgradeConfig.icon();
      Applicable applicable = upgradeConfig.applicable();

      boolean shouldGrow;
      int currentLevel = switch (affectType) {
        case SELF -> sessionContext.getPlayerUpgrade(uniqueId, upgradeType);
        case TEAM -> sessionContext.getTeamUpgrade(upgradeType);
      };

      shouldGrow = currentLevel >= level;

      ItemStackBuilder item = ItemStackBuilder.from(icon)
        .setItemStack(Objects.requireNonNull(material.parseItem(), String.format("Material from the %s cannot be null!", material.name())));

      if (shouldGrow) {
        item = item.addGlowEffect();
      }

      Icon builtIcon = FileElement.none(item)
        .replace(new Placeholder("%level%", level))
        .replace(new Placeholder("%required-points%", requiredPoints))
        .clickableItem()
        .whenClick(clickEvent -> {
          if (level <= currentLevel) {
            return;
          }

          if (currentLevel + 1 != level) {
            player.sendMessage(LanguageFile.shouldOneByOne.build());
            return;
          }

          if (pointManager.hasPoints(player, requiredPoints)) {
            player.sendMessage(LanguageFile.upgradeSuccess.build());
            pointManager.removePoints(player, requiredPoints);

            switch (affectType) {
              case SELF -> {
                sessionContext.upgradePlayer(uniqueId, upgradeType);
                applicable.apply(player);
              }
              case TEAM -> {
                sessionContext.upgradeTeam(upgradeType);
                sessionContext.getPlayers().forEach(applicable::apply);
              }
            }

            contents.notifyUpdate();
          } else {
            player.sendMessage(LanguageFile.notEnoughPoints.build());
          }
        });

      icons[level - 1] = builtIcon;
    });

    pagination.setIcons(icons);
    pagination.addToIterator(contents.newIterator(SlotIterator.Type.HORIZONTAL, 0, 0));
    if (icons.length > pageItems) {
      FileElement.insert(previous, 5, 3, clickEvent -> contents.openPrevious())
        .place(contents);
      FileElement.insert(next, 5, 5, clickEvent -> contents.openNext())
        .place(contents);
    }

    backButton
      .addEvent(clickEvent -> UpgradeAffectSelectMenu.open(player, sessionContext))
      .place(contents);
  }
}
