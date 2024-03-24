package me.despical.whackme.handlers.setup;

import me.despical.commons.compat.XMaterial;
import me.despical.commons.item.ItemBuilder;
import me.despical.inventoryframework.Gui;
import me.despical.inventoryframework.GuiItem;
import me.despical.inventoryframework.pane.StaticPane;
import me.despical.whackme.WhackMe;
import me.despical.whackme.arena.Arena;
import me.despical.whackme.handlers.setup.components.ArenaRegisterComponent;
import me.despical.whackme.handlers.setup.components.SetupComponent;
import me.despical.whackme.handlers.setup.components.MainComponents;
import org.bukkit.entity.Player;

/**
 * @author Despical
 * <p>
 * Created at 20.06.2022
 */
public class SetupInventory {

	private final Gui gui;
	private final Player player;
	private final Arena arena;
	private final WhackMe plugin;

	public SetupInventory(WhackMe plugin, Arena arena, Player player) {
		this.plugin = plugin;
		this.arena = arena;
		this.player = player;
		this.gui = new Gui(plugin, 3, "       Whack Me Arena Editor");
		this.gui.setOnGlobalClick(e -> e.setCancelled(true));
		prepareGui();
	}

	private void prepareGui() {
		StaticPane pane = new StaticPane(9, 3);
		ItemBuilder registeredItem = new ItemBuilder(XMaterial.GREEN_STAINED_GLASS_PANE).name("&aArena Validation Successful"), notRegisteredItem = new ItemBuilder(XMaterial.BLACK_STAINED_GLASS_PANE).name("&cArena Validation Not Finished Yet");
		pane.fillWith(arena.isReady() ? registeredItem.build() : notRegisteredItem.build());
		pane.fillProgressBorder(GuiItem.of(registeredItem.build()), GuiItem.of(notRegisteredItem.build()), arena.isReady() ? 100 : 0);

		this.gui.addPane(pane);

		SetupComponent spawnComponents = new MainComponents(this);
		spawnComponents.injectComponents(pane);

		SetupComponent arenaRegistryComponents = new ArenaRegisterComponent(this);
		arenaRegistryComponents.injectComponents(pane);
	}

	public void openInventory() {
		gui.show(player);
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

	public void closeInventory() {
		plugin.getServer().getScheduler().runTaskLater(plugin, () -> player.closeInventory(), 1L);
	}
}