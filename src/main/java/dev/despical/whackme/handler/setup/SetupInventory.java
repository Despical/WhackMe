package dev.despical.whackme.handler.setup;

import dev.despical.inventoryframework.Gui;
import dev.despical.inventoryframework.pane.PaginatedPane;
import dev.despical.whackme.WhackMe;
import dev.despical.whackme.arena.Arena;
import dev.despical.whackme.handler.setup.components.MainComponents;
import dev.despical.whackme.handler.setup.components.PointBlockAmountComponents;
import dev.despical.whackme.handler.setup.components.SetupComponent;
import org.bukkit.entity.Player;

/**
 * @author Despical
 * <p>
 * Created at 20.06.2022
 */
public class SetupInventory {

    private final PaginatedPane paginatedPane;
    private final Gui gui;
    private final Player player;
    private final Arena arena;
    private final WhackMe plugin;

    public SetupInventory(WhackMe plugin, Arena arena, Player player) {
        this.plugin = plugin;
        this.arena = arena;
        this.player = player;
        this.gui = new Gui(plugin, 4, "       Whack Me Arena Editor");
        this.gui.setOnGlobalClick(e -> e.setCancelled(true));
        this.paginatedPane = new PaginatedPane(9, 3);
        this.prepareGui();
        this.gui.show(player);
    }

    public SetupInventory(WhackMe plugin, Arena arena, Player player, String title) {
        this.plugin = plugin;
        this.arena = arena;
        this.player = player;
        this.gui = new Gui(plugin, 3, title);
        this.gui.setOnGlobalClick(e -> e.setCancelled(true));
        this.paginatedPane = new PaginatedPane(9, 3);
        this.prepareGui();
        this.paginatedPane.setPage(1);
        this.gui.show(player);
    }

    private void prepareGui() {
        gui.addPane(paginatedPane);

        SetupComponent spawnComponents = new MainComponents(this);
        spawnComponents.injectComponents(paginatedPane);

        SetupComponent amountComponents = new PointBlockAmountComponents(this);
        amountComponents.injectComponents(paginatedPane);
    }

    public WhackMe getPlugin() {
        return plugin;
    }

    public Arena getArena() {
        return arena;
    }

    public Player getPlayer() {
        return player;
    }

    public Gui getGui() {
        return gui;
    }

    public void closeInventory() {
        plugin.getServer().getScheduler().runTask(plugin, () -> player.closeInventory());
    }
}
