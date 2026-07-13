package dev.despical.whackme.setup.pages;

import dev.despical.fileitems.SpecialItem;
import dev.despical.inventoryframework.Gui;
import dev.despical.inventoryframework.GuiItem;
import dev.despical.inventoryframework.pane.PaginatedPane;
import dev.despical.inventoryframework.pane.StaticPane;
import dev.despical.whackme.arena.Arena;
import dev.despical.whackme.arena.options.ArenaKeys;
import dev.despical.whackme.setup.SetupMenu;
import dev.despical.whackme.setup.SetupPage;
import dev.despical.whackme.util.Utils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.function.Consumer;

/**
 * @author Despical
 * <p>
 * Created at 02.06.2026
 */
public class LocationsPage extends SetupPage {

    public LocationsPage(SetupMenu menu) {
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

        pane.addItem(createStartLocationItem(), 3, 1);
        pane.addItem(createEndLocationItem(), 5, 1);
        pane.addItem(createGoBackItem(), 8, 3);
    }

    private GuiItem createStartLocationItem() {
        SpecialItem item = itemManager.getItem("start");
        return GuiItem.of(item.getItemStack(), createLocationConsumer(item, true));
    }

    private GuiItem createEndLocationItem() {
        SpecialItem item = itemManager.getItem("end");
        return GuiItem.of(item.getItemStack(), createLocationConsumer(item, false));
    }

    private Consumer<InventoryClickEvent> createLocationConsumer(SpecialItem item, boolean start) {
        return event -> {
            Player player = (Player) event.getWhoClicked();

            Location playerLoc = player.getLocation();
            Location targetLocation;

            if (event.isShiftClick()) {
                targetLocation = playerLoc.getBlock().getLocation().add(0.5, 0, 0.5);
                targetLocation.setYaw(playerLoc.getYaw());

                if (start) {
                    for (int[] array : Utils.DIRECTIONS) {
                        targetLocation.clone().add(array[0], 0, array[1]).getBlock().setType(Material.END_PORTAL_FRAME);
                    }

                    Arena arena = menu.getArena();
                    arena.setOption(ArenaKeys.CUSTOM, false);

                    player.playSound(player.getLocation(), Sound.BLOCK_END_PORTAL_FRAME_FILL,1f, 1f);
                }
            } else {
                targetLocation = playerLoc.clone();
            }

            targetLocation.setPitch(0);

            if (start) {
                if (!arena.getOption(ArenaKeys.CUSTOM) && !Utils.isSurroundedBy(targetLocation)) {
                    chatManager.sendRawMessage(player, item.getCustomKey("invalid-message"));
                    player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.7f, 1.2f);
                    return;
                }

                arena.setOption(ArenaKeys.START_LOCATION, targetLocation);

                if (Utils.isSurroundedBy(targetLocation)) {
                    arena.setOption(ArenaKeys.PORTAL_LOCATIONS, Utils.getLocationsSurroundedBy(targetLocation));
                }
            } else {
                arena.setOption(ArenaKeys.END_LOCATION, targetLocation);
            }

            player.playSound(player.getLocation(), Sound.BLOCK_LODESTONE_PLACE, 1f, 1f);
            chatManager.sendRawMessage(player, item.getCustomKey("message"));
            
            menu.close();
        };
    }
}
