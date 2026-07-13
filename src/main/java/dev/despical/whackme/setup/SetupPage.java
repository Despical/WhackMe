package dev.despical.whackme.setup;

import dev.despical.fileitems.ItemManager;
import dev.despical.inventoryframework.Gui;
import dev.despical.inventoryframework.GuiItem;
import dev.despical.whackme.WhackMe;
import dev.despical.whackme.arena.Arena;
import dev.despical.whackme.chat.ChatManager;
import dev.despical.whackme.menu.Page;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * @author Despical
 * <p>
 * Created at 2.06.2026
 */
public abstract class SetupPage implements Page {

    protected static final WhackMe plugin = WhackMe.getInstance();

    protected final SetupMenu menu;
    protected final Arena arena;
    protected final ItemManager itemManager;
    protected final ChatManager chatManager;

    protected SetupPage(SetupMenu menu) {
        this.menu = menu;
        this.arena = menu.getArena();
        this.itemManager = plugin.getItemManager();
        this.chatManager = plugin.getChatManager();
    }

    public void configure(Gui gui) {
        gui.setOnGlobalClick(event -> event.setCancelled(true));
    }

    protected final GuiItem createGoBackItem() {
        ItemStack item = itemManager.getItem("go-back").getItemStack();

        return GuiItem.of(item, event -> {
            Player player = (Player) event.getWhoClicked();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 0.6f);

            menu.setPage(0);
        });
    }
}
