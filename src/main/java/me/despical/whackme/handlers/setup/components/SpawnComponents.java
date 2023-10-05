package me.despical.whackme.handlers.setup.components;

import me.despical.commons.compat.XMaterial;
import me.despical.commons.configuration.ConfigUtils;
import me.despical.commons.item.ItemBuilder;
import me.despical.commons.serializer.InventorySerializer;
import me.despical.commons.serializer.LocationSerializer;
import me.despical.inventoryframework.GuiItem;
import me.despical.inventoryframework.pane.StaticPane;
import me.despical.whackme.handlers.setup.SetupInventory;
import me.despical.whackme.utils.Utils;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

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
		final var user = plugin.getUserManager().getUser(player);

		pane.addItem(GuiItem.of(new ItemBuilder(user.isInEditingMode() ? XMaterial.ENDER_EYE : XMaterial.ENDER_PEARL)
			.name("&e&lSet Custom Portals")
			.build(), e -> {

			player.closeInventory();

			Runnable leaveEditing = () -> {
				user.setEditingMode(false);
				user.sendRawMessage("&e✔ Completed | &aYou've left the editing mode.");

				player.getInventory().clear();
				player.closeInventory();

				InventorySerializer.loadInventory(plugin, player);
			};

			if (user.isInEditingMode()) {
				leaveEditing.run();
				return;
			}

			InventorySerializer.saveInventoryToFile(plugin, player);

			var inventory = player.getInventory();
			inventory.clear();
			inventory.setItem(4, new ItemBuilder(XMaterial.BARRIER).name("&c&lLeave Editing Mode").build());

			var item = XMaterial.END_PORTAL_FRAME.parseItem();

			for (int slot : List.of(0, 1, 2, 3, 5, 6, 7, 8)) {
				inventory.setItem(slot, item);
			}

			user.setEditingMode(true);
			user.sendRawMessage("&e✔ Info | &aIn editing mode you can place end portals to anywhere you want and can leave by clicking barrier item.");

			var locations = new HashSet<>(arena.getLocations());

			plugin.getServer().getPluginManager().registerEvents(new Listener() {

				@EventHandler
				public void onLeaveEditing(PlayerInteractEvent event) {
					final var user = plugin.getUserManager().getUser(event.getPlayer());

					if (!user.isInEditingMode()) return;
					if (event.getAction() == Action.PHYSICAL) return;
					if (event.getItem() == null || event.getItem().getType() != Material.BARRIER) return;

					event.setCancelled(true);

					leaveEditing.run();

					HandlerList.unregisterAll(this);

					arena.setLocations(new ArrayList<>(locations));

					config.set("instances.%s.portalLocations".formatted(arena.getId()), locations.stream().map(LocationSerializer::toString).collect(Collectors.toList()));
					ConfigUtils.saveConfig(plugin, config, "arenas");
				}

				@EventHandler
				public void onPortalPlacing(BlockPlaceEvent event) {
					final var user = plugin.getUserManager().getUser(event.getPlayer());

					if (!user.isInEditingMode()) return;
					if (event.getBlock().getType() != Material.END_PORTAL_FRAME) return;

					locations.add(event.getBlock().getLocation());
				}

				@EventHandler
				public void onPortalBreaking(BlockBreakEvent event) {
					final var user = plugin.getUserManager().getUser(event.getPlayer());

					if (!user.isInEditingMode()) return;
					if (event.getBlock().getType() != Material.END_PORTAL_FRAME) return;

					locations.remove(event.getBlock().getLocation());
				}
			}, plugin);
		}), 0, 0);

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

			final var location = player.getLocation();

			arena.setCustom(true);

			if (e.isShiftClick()) {
				final var portal = Utils.END_PORTAL_FRAME.getType();

				for (int[] array : Utils.DIRECTIONS) {
					location.clone().add(array[0], 0, array[1]).getBlock().setType(portal);
				}

				arena.setCustom(false);
				config.set(path + "custom", false);
			} else {
				player.closeInventory(); // to prevent shift click bugs
			}

			if (!arena.isCustom() && !Utils.isSurroundedBy(location)) {
				user.sendRawMessage("&c&l✖ &cWarning | Blocks around the start location must be end portal frame!");
				return;
			}

			arena.setStartLocation(location);
			user.sendRawMessage("&e✔ Completed | &aStart location for arena &e" + arena.getId() + " &aset at your location!");

			config.set(path + "custom", arena.isCustom());
			config.set(path + "startLocation", LocationSerializer.toString(location));
			ConfigUtils.saveConfig(plugin, config, "arenas");
		}), 3, 1);

		pane.addItem(GuiItem.of(new ItemBuilder(XMaterial.REDSTONE_BLOCK)
			.name("        &e&lSet Ending Location        ")
			.lore("&7Click to set ending location on")
			.lore("&7the place where you are standing.")
			.lore("&8(location where players will be")
			.lore("&8teleported after the reloading)")
			.lore("", isOptionDoneBool(path + "endLocation"))
			.build(), e -> {

			player.closeInventory();

			user.sendRawMessage("&e✔ Completed | &aEnding location for arena &e" + arena.getId() + " &aset at your location!");

			final var location = player.getLocation().clone().add(.5, 0, .5);
			arena.setEndLocation(location);

			config.set(path + "endLocation", LocationSerializer.toString(location));
			ConfigUtils.saveConfig(plugin, config, "arenas");
		}), 5, 1);
	}
}