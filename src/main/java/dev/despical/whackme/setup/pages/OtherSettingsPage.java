package dev.despical.whackme.setup.pages;

import dev.despical.fileitems.SpecialItem;
import dev.despical.inventoryframework.Gui;
import dev.despical.inventoryframework.GuiItem;
import dev.despical.inventoryframework.pane.PaginatedPane;
import dev.despical.inventoryframework.pane.StaticPane;
import dev.despical.whackme.arena.options.ArenaKeys;
import dev.despical.whackme.arena.options.ArenaOption;
import dev.despical.whackme.game.Game;
import dev.despical.whackme.setup.SetupMenu;
import dev.despical.whackme.setup.SetupPage;
import dev.despical.whackme.util.Var;
import net.kyori.adventure.text.Component;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

/**
 * @author Despical
 * <p>
 * Created at 15.07.2026
 */
public final class OtherSettingsPage extends SetupPage {

    public OtherSettingsPage(SetupMenu menu) {
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

        pane.addItem(createScoreboardToggle(), 3, 1);
        pane.addItem(createBossBarToggle(), 5, 1);
        pane.addItem(createGoBackItem(), 8, 2);
    }

    private GuiItem createScoreboardToggle() {
        return createToggleItem(
            "scoreboard-toggle",
            "%scoreboard_status%",
            ArenaKeys.ARENA_SCOREBOARD_ENABLED,
            () -> {
                Game game = arena.getGame();
                if (game != null) {
                    game.getScoreboardManager().refresh();
                }
            }
        );
    }

    private GuiItem createBossBarToggle() {
        return createToggleItem(
            "bossbar-toggle",
            "%bossbar_status%",
            ArenaKeys.ARENA_BOSS_BAR_ENABLED,
            () -> {
                Game game = arena.getGame();
                if (game != null) {
                    game.getBossBarManager().update();
                }
            }
        );
    }

    private GuiItem createToggleItem(String itemKey, String statusPlaceholder, ArenaOption<Boolean> option, Runnable refreshDisplay) {
        SpecialItem specialItem = itemManager.getItem(itemKey);
        ItemStack item = specialItem.getItemStack().clone();
        applyStatus(item, statusPlaceholder, arena.getOption(option));

        return GuiItem.of(item, event -> {
            Player player = (Player) event.getWhoClicked();
            boolean enabled = !arena.getOption(option);

            arena.setOption(option, enabled);
            refreshDisplay.run();

            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, enabled ? 1.4f : 0.8f);
            menu.setPage(4);
        });
    }

    private void applyStatus(ItemStack item, String placeholder, boolean enabled) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasLore()) {
            return;
        }

        List<Component> lore = meta.lore();
        if (lore == null) {
            return;
        }

        String status = enabled ? "<#00E676><bold>ENABLED" : "<#FF5252><bold>DISABLED";
        Var statusVar = Var.of(placeholder, status);
        meta.lore(lore.stream()
            .map(line -> chatManager.replaceVarsInComponent(line, statusVar))
            .toList());
        item.setItemMeta(meta);
    }
}
