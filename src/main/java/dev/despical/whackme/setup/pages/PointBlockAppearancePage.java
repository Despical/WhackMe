package dev.despical.whackme.setup.pages;

import dev.despical.fileitems.SpecialItem;
import dev.despical.inventoryframework.Gui;
import dev.despical.inventoryframework.GuiItem;
import dev.despical.inventoryframework.pane.PaginatedPane;
import dev.despical.inventoryframework.pane.StaticPane;
import dev.despical.whackme.arena.blocks.PointBlockType;
import dev.despical.whackme.setup.SetupMenu;
import dev.despical.whackme.setup.SetupPage;
import dev.despical.whackme.util.Var;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * @author Despical
 * <p>
 * Created at 03.06.2026
 */
public class PointBlockAppearancePage extends SetupPage {

    private final SpecialItem specialItem;

    public PointBlockAppearancePage(SetupMenu menu) {
        super(menu);
        this.specialItem = itemManager.getItem("point-block-appearance");
    }

    @Override
    public void configure(Gui gui) {
        gui.setOnGlobalClick(event -> {
            if (event.getClickedInventory() == null) {
                return;
            }

            boolean topInventory = event.getClickedInventory().equals(event.getView().getTopInventory());
            if (topInventory) {
                event.setCancelled(true);
                return;
            }

            event.setCancelled(event.isShiftClick() || event.getClick().isKeyboardClick());
        });
    }

    @Override
    public void beforeOpening(Gui gui) {
        gui.setRows(3);
    }

    @Override
    public void injectItems(PaginatedPane paginatedPane) {
        StaticPane pane = new StaticPane(9, 3);
        paginatedPane.addPane(0, pane);

        pane.addItem(createBlockItem(PointBlockType.GREEN_BLOCK, "green-name"), 2, 1);
        pane.addItem(createBlockItem(PointBlockType.RED_BLOCK, "red-name"), 4, 1);
        pane.addItem(createBlockItem(PointBlockType.GRAY_BLOCK, "gray-name"), 6, 1);
        pane.addItem(createGoBackItem(), 8, 2);
    }

    private GuiItem createBlockItem(PointBlockType type, String nameKey) {
        ItemStack item = arena.getOption(type.getArenaOption()).clone();
        item.setAmount(1);
        applyPreviewMeta(item, specialItem.getCustomKey(nameKey));

        return GuiItem.of(item, event -> {
            Player player = (Player) event.getWhoClicked();
            ItemStack cursor = event.getCursor();
            String blockName = specialItem.getCustomKey(nameKey);

            if (cursor.getType() == Material.AIR) {
                chatManager.sendRawMessage(player, specialItem.getCustomKey("missing-cursor"));
                player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.7f, 1.2f);
                return;
            }

            ItemStack replacement = cursor.clone();
            replacement.setAmount(1);
            arena.setOption(type.getArenaOption(), replacement);

            chatManager.sendRawMessage(player, specialItem.getCustomKey("updated-message"),
                Var.of("%block_name%", blockName),
                Var.of("%arena_id%", arena.getId()));
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1.6f);
            menu.setPage(3);
        });
    }

    private void applyPreviewMeta(ItemStack item, String title) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return;
        }

        meta.displayName(chatManager.parseMessage(title));
        meta.lore(chatManager.parseList(specialItem.getCustomKey("preview-lore")));
        item.setItemMeta(meta);
    }
}
