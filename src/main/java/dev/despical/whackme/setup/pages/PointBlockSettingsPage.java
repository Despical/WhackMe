package dev.despical.whackme.setup.pages;

import dev.despical.fileitems.SpecialItem;
import dev.despical.inventoryframework.Gui;
import dev.despical.inventoryframework.GuiItem;
import dev.despical.inventoryframework.pane.PaginatedPane;
import dev.despical.inventoryframework.pane.StaticPane;
import dev.despical.whackme.arena.options.ArenaKeys;
import dev.despical.whackme.setup.SetupMenu;
import dev.despical.whackme.setup.SetupPage;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

/**
 * @author Despical
 * <p>
 * Created at 02.06.2026
 */
public class PointBlockSettingsPage extends SetupPage {

    /**
     * @author Despical
     * <p>
     * Created at 02.06.2026
     */
    private record PointAmountState(int minimum, int maximum, boolean clampedToPortalLimit) {
    }

    public PointBlockSettingsPage(SetupMenu menu) {
        super(menu);
    }

    @Override
    public void beforeOpening(Gui gui) {
        gui.setRows(3);
    }

    @Override
    public void injectItems(PaginatedPane paginatedPane) {
        StaticPane pane = new StaticPane(9, 3);
        paginatedPane.addPane(0, pane);

        pane.addItem(createMinPointsItem(), 3, 1);
        pane.addItem(createMaxPointsItem(), 5, 1);
        pane.addItem(createGoBackItem(), 8, 2);
    }

    private GuiItem createMinPointsItem() {
        SpecialItem specialItem = itemManager.getItem("minimum-point-blocks");
        ItemStack item = specialItem.asItemBuilder().amount(Math.max(1, arena.getOption(ArenaKeys.MINIMUM_POINTS))).build();
        updateCurrentValueLore(item, arena.getOption(ArenaKeys.MINIMUM_POINTS));

        return GuiItem.of(item, event -> {
            Player player = (Player) event.getWhoClicked();
            int amount = adjustAmount(event.getClick(), arena.getOption(ArenaKeys.MINIMUM_POINTS));
            applyPointAmountState(player, specialItem.getCustomKey("minimum-above-portals"), normalizePointAmounts(amount, arena.getOption(ArenaKeys.MAXIMUM_POINTS), true));
        });
    }

    private GuiItem createMaxPointsItem() {
        SpecialItem specialItem = itemManager.getItem("maximum-point-blocks");
        ItemStack item = specialItem.asItemBuilder().amount(arena.getOption(ArenaKeys.MAXIMUM_POINTS)).build();
        updateCurrentValueLore(item, arena.getOption(ArenaKeys.MAXIMUM_POINTS));

        return GuiItem.of(item, event -> {
            Player player = (Player) event.getWhoClicked();
            int amount = adjustAmount(event.getClick(), arena.getOption(ArenaKeys.MAXIMUM_POINTS));
            applyPointAmountState(player, specialItem.getCustomKey("maximum-above-portals"), normalizePointAmounts(arena.getOption(ArenaKeys.MINIMUM_POINTS), amount, false));
        });
    }

    private int adjustAmount(ClickType click, int current) {
        if (click.isLeftClick()) {
            return current + 1;
        }

        if (click.isRightClick()) {
            return current - 1;
        }

        return current;
    }

    private PointAmountState normalizePointAmounts(int minimum, int maximum, boolean minimumChanged) {
        minimum = Math.max(1, minimum);
        maximum = Math.max(1, maximum);

        if (maximum < minimum) {
            if (minimumChanged) {
                maximum = minimum;
            } else {
                minimum = maximum;
            }
        }

        int portalCount = arena.getOption(ArenaKeys.PORTAL_LOCATIONS).size();
        boolean clampedToPortalLimit = false;

        if (portalCount > 0) {
            if (minimum > portalCount) {
                minimum = portalCount;
                maximum = portalCount;
                clampedToPortalLimit = true;
            } else if (maximum > portalCount) {
                maximum = portalCount;
                clampedToPortalLimit = true;
            }
        }

        return new PointAmountState(minimum, maximum, clampedToPortalLimit);
    }

    private void applyPointAmountState(Player player, String portalLimitMessage, PointAmountState state) {
        arena.setOption(ArenaKeys.MINIMUM_POINTS, state.minimum());
        arena.setOption(ArenaKeys.MAXIMUM_POINTS, state.maximum());

        if (state.clampedToPortalLimit()) {
            chatManager.sendRawMessage(player, portalLimitMessage);
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
        } else {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
        }

        menu.setPage(2);
    }

    private void updateCurrentValueLore(ItemStack item, int value) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasLore()) {
            return;
        }

        List<net.kyori.adventure.text.Component> lore = meta.lore();
        if (lore == null) {
            return;
        }

        lore = lore.stream()
            .map(line -> chatManager.replaceVarsInComponent(line, dev.despical.whackme.util.Var.of("%current_value%", value)))
            .toList();

        meta.lore(lore);
        item.setItemMeta(meta);
    }
}
