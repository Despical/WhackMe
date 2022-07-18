package me.despical.whackme.handler.setup.components;

import me.despical.commons.compat.XMaterial;
import me.despical.commons.configuration.ConfigUtils;
import me.despical.commons.item.ItemBuilder;
import me.despical.commons.serializer.LocationSerializer;
import me.despical.inventoryframework.GuiItem;
import me.despical.inventoryframework.pane.StaticPane;
import me.despical.whackme.arena.Arena;
import me.despical.whackme.handler.setup.SetupInventory;
import me.despical.whackme.util.Utils;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * @author Despical
 * <p>
 * Created at 20.06.2022
 */
public class SpawnComponents implements SetupComponent {

	@Override
	public void injectComponents(SetupInventory setupInventory, StaticPane pane) {
		final Player player = setupInventory.getPlayer();
		final Arena arena = setupInventory.getArena();
		final String path = "instances." + arena.getId() + ".";

		pane.addItem(GuiItem.of(new ItemBuilder(XMaterial.IRON_BLOCK)
			.name("&e&lSet Start Location")
			.lore("&7Click to set start location on")
			.lore("&7the place where you are standing.")
			.lore("&8(location where players will be")
			.lore("&8teleported to join game)")
			.lore("", setupInventory.getSetupUtilities().isOptionDoneBool(path + "startLocation"))
			.build(), e -> {

			player.closeInventory();

			final Location location = player.getLocation();

			if (!Utils.isSurroundedBy(location)) {
				player.sendMessage(chatManager.coloredRawMessage("&c&l✖ &cWarning | Blocks around the start location must be end portal frame!"));
				return;
			}

			player.sendMessage(chatManager.coloredRawMessage("&e✔ Completed | &aStart location for arena &e" + arena.getId() + " &aset at your location!"));

			arena.setStartLocation(location);

			config.set(path + "startLocation", LocationSerializer.toString(location));
			ConfigUtils.saveConfig(plugin, config, "arenas");
		}), 3, 1);

		pane.addItem(GuiItem.of(new ItemBuilder(XMaterial.REDSTONE_BLOCK)
			.name("&e&lSet Ending Location")
			.lore("&7Click to set ending location on")
			.lore("&7the place where you are standing.")
			.lore("&8(location where players will be")
			.lore("&8teleported after the reloading)")
			.lore("", setupInventory.getSetupUtilities().isOptionDoneBool(path + "endLocation"))
			.build(), e -> {

			player.closeInventory();
			player.sendMessage(chatManager.coloredRawMessage("&e✔ Completed | &aEnding location for arena &e" + arena.getId() + " &aset at your location!"));

			final Location location = player.getLocation().add(.5, 0, .5);
			arena.setEndLocation(location);

			config.set(path + "endLocation", LocationSerializer.toString(location));
			ConfigUtils.saveConfig(plugin, config, "arenas");
		}), 5, 1);
	}
}