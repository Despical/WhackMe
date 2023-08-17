package me.despical.whackme.handlers.setup.components;

import me.despical.commons.compat.XMaterial;
import me.despical.commons.configuration.ConfigUtils;
import me.despical.commons.item.ItemBuilder;
import me.despical.commons.serializer.LocationSerializer;
import me.despical.inventoryframework.GuiItem;
import me.despical.inventoryframework.pane.StaticPane;
import me.despical.whackme.handlers.setup.SetupInventory;
import me.despical.whackme.utils.Utils;

/**
 * @author Despical
 * <p>
 * Created at 20.06.2022
 */
public class SpawnComponents implements SetupComponent {

	@Override
	public void injectComponents(SetupInventory setupInventory, StaticPane pane) {
		final var player = setupInventory.getPlayer();
		final var arena = setupInventory.getArena();
		final var path = "instances." + arena.getId() + ".";

		pane.addItem(GuiItem.of(new ItemBuilder(XMaterial.IRON_BLOCK)
			.name("&e&l        Set Start Location        ")
			.lore("&7Click to set start location on")
			.lore("&7the place where you are standing.")
			.lore("&8(location where players will be")
			.lore("&8teleported to join game)")
			.lore("", isOptionDoneBool(path + "startLocation"), "")
			.lore("&8• &cShift Click to spawn end portals")
			.lore("&caround you without placing manually.")
			.build(), e -> {

			player.closeInventory();

			final var location = player.getLocation();

			if (e.isShiftClick()) {
				final var portal = Utils.END_PORTAL_FRAME.getType();

				for (int[] array : Utils.DIRECTIONS) {
					location.clone().add(array[0], 0, array[1]).getBlock().setType(portal);
				}
			}

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
			.name("      &e&lSet Ending Location      ")
			.lore("&7Click to set ending location on")
			.lore("&7the place where you are standing.")
			.lore("&8(location where players will be")
			.lore("&8teleported after the reloading)")
			.lore("", isOptionDoneBool(path + "endLocation"))
			.build(), e -> {

			player.closeInventory();
			player.sendMessage(chatManager.coloredRawMessage("&e✔ Completed | &aEnding location for arena &e" + arena.getId() + " &aset at your location!"));

			final var location = player.getLocation().add(.5, 0, .5);
			arena.setEndLocation(location);

			config.set(path + "endLocation", LocationSerializer.toString(location));
			ConfigUtils.saveConfig(plugin, config, "arenas");
		}), 5, 1);
	}
}