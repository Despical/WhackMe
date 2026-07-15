package dev.despical.whackme.setup.pages;

import dev.despical.fileitems.SpecialItem;
import dev.despical.inventoryframework.Gui;
import dev.despical.inventoryframework.GuiItem;
import dev.despical.inventoryframework.pane.PaginatedPane;
import dev.despical.inventoryframework.pane.StaticPane;
import dev.despical.whackme.arena.options.ArenaKeys;
import dev.despical.whackme.setup.SetupMenu;
import dev.despical.whackme.setup.SetupPage;
import dev.despical.whackme.util.ItemUtils;
import dev.despical.whackme.util.Var;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * @author Despical
 * <p>
 * Created at 15.07.2026
 */
public final class ResetArenaRecordConfirmationPage extends SetupPage {

    public ResetArenaRecordConfirmationPage(SetupMenu menu) {
        super(menu);
    }

    @Override
    public void beforeOpening(Gui gui) {
        gui.setRows(4);
    }

    @Override
    public void injectItems(PaginatedPane paginatedPane) {
        StaticPane pane = new StaticPane(9, 4);
        paginatedPane.addPane(0, pane);

        pane.addItem(createRecordPreview(), 4, 1);
        pane.addItem(createConfirmItem(), 2, 2);
        pane.addItem(createCancelItem(), 6, 2);
    }

    private GuiItem createRecordPreview() {
        SpecialItem specialItem = itemManager.getItem("arena-record-reset");
        String holder = arena.getOption(ArenaKeys.RECORD_HOLDER);
        ItemStack item = ItemUtils.formatItemStack(
            specialItem,
            Var.of("%record_holder%", holder),
            Var.of("%record_score%", arena.getOption(ArenaKeys.RECORD_SCORE))
        );

        ItemUtils.applyArenaRecordResetHead(item, holder);
        return GuiItem.of(item, event -> event.setCancelled(true));
    }

    private GuiItem createConfirmItem() {
        SpecialItem specialItem = itemManager.getItem("confirm-arena-record-reset");
        ItemStack item = specialItem.getItemStack();

        return GuiItem.of(item, event -> {
            Player player = (Player) event.getWhoClicked();

            arena.setOption(ArenaKeys.RECORD_HOLDER, "None");
            arena.setOption(ArenaKeys.RECORD_SCORE, 0);

            if (arena.getGame() != null) {
                arena.getGame().getScoreboardManager().update();
            }

            menu.close();
            chatManager.sendRawMessage(
                player,
                specialItem.getCustomKey("message"),
                Var.of("%arena_id%", arena.getId())
            );
            player.playSound(player.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 1f, 0.7f);
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1.3f);
        });
    }

    private GuiItem createCancelItem() {
        ItemStack item = itemManager.getItem("cancel-arena-record-reset").getItemStack();

        return GuiItem.of(item, event -> {
            Player player = (Player) event.getWhoClicked();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 0.8f);
            menu.setPage(0);
        });
    }
}
