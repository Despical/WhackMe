package me.despical.whackme.handlers.setup.components;

import me.despical.commons.compat.XMaterial;
import me.despical.commons.configuration.ConfigUtils;
import me.despical.commons.item.ItemBuilder;
import me.despical.commons.serializer.InventorySerializer;
import me.despical.commons.serializer.LocationSerializer;
import me.despical.inventoryframework.GuiItem;
import me.despical.inventoryframework.pane.StaticPane;
import me.despical.whackme.WhackMe;
import me.despical.whackme.arena.Arena;
import me.despical.whackme.handlers.setup.SetupInventory;
import me.despical.whackme.handlers.sign.SignManager;
import me.despical.whackme.user.User;
import me.despical.whackme.utils.Utils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Despical
 * <p>
 * Created at 20.06.2022
 */
public class MainComponents extends SetupComponent {

	public MainComponents(SetupInventory setup) {
		super(setup);
	}

	@Override
	public void injectComponents(StaticPane pane) {
		WhackMe plugin = setup.getPlugin();
		Player player = setup.getPlayer();
		Arena arena = setup.getArena();
		String path = "instances." + arena.getId() + ".";
		User user = plugin.getUserManager().getUser(player);

		pane.addItem(GuiItem.of(new ItemBuilder(user.isInEditingMode() ? XMaterial.ENDER_EYE : XMaterial.ENDER_PEARL)
			.name("&e&lSet Custom Portals")
			.build(), e -> {

			setup.closeInventory();

			Runnable leaveEditing = () -> {
				user.setEditingMode(false);
				user.sendRawMessage("&e✔ Completed | &aYou've left the editing mode.");

				player.getInventory().clear();
				setup.closeInventory();

				InventorySerializer.loadInventory(plugin, player);
			};

			if (user.isInEditingMode()) {
				leaveEditing.run();
				return;
			}

			InventorySerializer.saveInventoryToFile(plugin, player);

			Inventory inventory = player.getInventory();
			inventory.clear();
			inventory.setItem(4, new ItemBuilder(XMaterial.BARRIER).name("&c&lLeave Editing Mode").build());

			ItemStack item = XMaterial.END_PORTAL_FRAME.parseItem();

			for (int slot : Arrays.asList(0, 1, 2, 3, 5, 6, 7, 8)) {
				inventory.setItem(slot, item);
			}

			arena.setCustom(true);

			user.setEditingMode(true);
			user.sendRawMessage("&e✔ Info | &aIn editing mode you can place end portals to anywhere you want and can leave by clicking barrier item.");

			Set<Location> locations = new HashSet<>(arena.getLocations());

			plugin.getServer().getPluginManager().registerEvents(new Listener() {

				@EventHandler
				public void onLeaveEditing(PlayerInteractEvent event) {
					User user = plugin.getUserManager().getUser(event.getPlayer());

					if (!user.isInEditingMode()) return;
					if (event.getAction() == Action.PHYSICAL) return;
					if (event.getItem() == null || event.getItem().getType() != Material.BARRIER) return;

					event.setCancelled(true);

					leaveEditing.run();

					HandlerList.unregisterAll(this);

					arena.setLocations(new ArrayList<>(locations));

					FileConfiguration config = ConfigUtils.getConfig(plugin, "arenas");
					config.set(String.format("instances.%s.portalLocations", arena.getId()), locations.stream().map(LocationSerializer::toString).collect(Collectors.toList()));
					ConfigUtils.saveConfig(plugin, config, "arenas");
				}

				@EventHandler
				public void onPortalPlacing(BlockPlaceEvent event) {
					final User user = plugin.getUserManager().getUser(event.getPlayer());

					if (!user.isInEditingMode()) return;
					if (event.getBlock().getType() != Material.END_PORTAL_FRAME) return;

					locations.add(event.getBlock().getLocation());
				}

				@EventHandler
				public void onPortalBreaking(BlockBreakEvent event) {
					final User user = plugin.getUserManager().getUser(event.getPlayer());

					if (!user.isInEditingMode()) return;
					if (event.getBlock().getType() != Material.END_PORTAL_FRAME) return;

					locations.remove(event.getBlock().getLocation());
				}
			}, plugin);
		}), 0, 0);

		pane.addItem(GuiItem.of(new ItemBuilder(XMaterial.IRON_BLOCK)
			.name("&e&l        Set Start Location       ")
			.lore("&7Click to set start location on")
			.lore("&7the place where you are standing.")
			.lore("&8(location where players will be")
			.lore("&8teleported to join game)")
			.lore("", isOptionDoneBool(path + "startLocation"), "")
			.lore("&8• &cShift Click to spawn end portals")
			.lore("&caround you without placing manually.")
			.build(), e -> {

			final Location location = player.getLocation();
			boolean reopenInventory = false;

			if (e.isShiftClick()) {
				final Material portal = Utils.END_PORTAL_FRAME.getType();

				for (int[] array : Utils.DIRECTIONS) {
					location.clone().add(array[0], 0, array[1]).getBlock().setType(portal);
				}

				arena.setCustom(false);

				reopenInventory = true;
			} else {
				setup.closeInventory();
			}

			if (!arena.isCustom() && !Utils.isSurroundedBy(location)) {
				user.sendRawMessage("&c&l✖ &cWarning | Blocks around the start location must be end portal frame!");
				return;
			}

			arena.setStartLocation(location);
			user.sendRawMessage("&e✔ Completed | &aStart location for arena &e" + arena.getId() + " &aset at your location!");

			FileConfiguration config = ConfigUtils.getConfig(plugin, "arenas");
			config.set(path + "custom", arena.isCustom());
			config.set(path + "startLocation", LocationSerializer.toString(location));
			config.set(path + "portalLocations", arena.getLocations().stream().map(LocationSerializer::toString).collect(Collectors.toList()));
			ConfigUtils.saveConfig(plugin, config, "arenas");

			if (reopenInventory)
				new SetupInventory(plugin, arena, player).openInventory();
		}), 2, 1);

		pane.addItem(GuiItem.of(new ItemBuilder(XMaterial.REDSTONE_BLOCK)
			.name("        &e&lSet Ending Location      ")
			.lore("&7Click to set ending location on")
			.lore("&7the place where you are standing.")
			.lore("&8(location where players will be")
			.lore("&8teleported after the reloading)")
			.lore("", isOptionDoneBool(path + "endLocation"))
			.build(), e -> {

			setup.closeInventory();

			user.sendRawMessage("&e✔ Completed | &aEnding location for arena &e" + arena.getId() + " &aset at your location!");

			final Location location = player.getLocation().clone().add(.5, 0, .5);
			arena.setEndLocation(location);

			FileConfiguration config = ConfigUtils.getConfig(plugin, "arenas");
			config.set(path + "endLocation", LocationSerializer.toString(location));
			ConfigUtils.saveConfig(plugin, config, "arenas");
		}), 6, 1);

		pane.addItem(GuiItem.of(new ItemBuilder(XMaterial.OAK_SIGN)
			.name("       &e&lAdd Game Sign")
			.lore("&7Target a sign and click this.")
			.build(), e -> {

			setup.closeInventory();

			Block block = user.getPlayer().getTargetBlock(null, 10);

			if (!(block.getState() instanceof Sign)) {
				user.sendRawMessage("&cYou are not looking at any sign block!");
				return;
			}

			SignManager signManager = plugin.getSignManager();

			if (signManager.isGameSign(block)) {
				user.sendRawMessage("&cThis sign is already a game sign!");
				return;
			}

			FileConfiguration config = ConfigUtils.getConfig(plugin, "arenas");
			List<String> locations = config.getStringList(path + "signs");
			locations.add(LocationSerializer.toString(block.getLocation()));

			config.set(path + "signs", locations);
			ConfigUtils.saveConfig(plugin, config, "arenas");

			signManager.addArenaSign(block, arena);
			signManager.updateSign(arena);

			user.sendRawMessage("&aArena sign has been created successfully!");
		}), 4, 1);
	}
}