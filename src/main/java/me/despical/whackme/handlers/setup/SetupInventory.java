package me.despical.whackme.handlers.setup;

import me.despical.inventoryframework.Gui;
import me.despical.inventoryframework.pane.PaginatedPane;
import me.despical.whackme.WhackMe;
import me.despical.whackme.arena.Arena;
import me.despical.whackme.handlers.setup.components.MainComponents;
import me.despical.whackme.handlers.setup.components.PointBlockAmountComponents;
import me.despical.whackme.handlers.setup.components.SetupComponent;
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
		this.gui.addPane(paginatedPane);

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
		plugin.getServer().getScheduler().runTaskLater(plugin, () -> player.closeInventory(), 1L);
	}
}