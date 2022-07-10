package me.despical.whackme.handler.setup.components;

import me.despical.commons.compat.XMaterial;
import me.despical.commons.configuration.ConfigUtils;
import me.despical.commons.item.ItemBuilder;
import me.despical.commons.serializer.LocationSerializer;
import me.despical.inventoryframework.GuiItem;
import me.despical.inventoryframework.pane.StaticPane;
import me.despical.whackme.arena.Arena;
import me.despical.whackme.arena.ArenaRegistry;
import me.despical.whackme.handler.setup.SetupInventory;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;

/**
 * @author Despical
 * <p>
 * Created at 21.06.2022
 */
public class ArenaRegisterComponent implements SetupComponent {

	@Override
	public void injectComponents(SetupInventory setupInventory, StaticPane pane) {
		Player player = setupInventory.getPlayer();
		Arena arena = setupInventory.getArena();
		ItemBuilder registeredItem;

		if (!arena.isReady()) {
			registeredItem = new ItemBuilder(XMaterial.FIREWORK_ROCKET)
				.name("&e&lRegister Arena - Finish Setup")
				.lore("&7Click this when you're done with configuration.")
				.lore("&7It will validate and register the arena.");
		} else {
			registeredItem = new ItemBuilder(Material.BARRIER)
				.name("&a&lArena Registered - Congratulations")
				.lore("&7This arena is already registered!")
				.lore("&7Good job, you went through whole setup!")
				.enchantment(Enchantment.ARROW_DAMAGE)
				.flag(ItemFlag.HIDE_ENCHANTS);
		}

		pane.addItem(GuiItem.of(registeredItem.build(), e -> {
			String path = "instances." + arena.getId() + ".";

			player.closeInventory();

			if (config.getBoolean(path + "ready")) {
				player.sendMessage(chatManager.coloredRawMessage("&a&l✔ &aThis arena was already validated and is ready to use!"));
				return;
			}

			String[] locations = {"startLocation", "endLocation"};

			for (String loc : locations) {
				if (!config.isSet(path + loc) || LocationSerializer.isDefaultLocation(config.getString(path + loc))) {
					player.sendMessage(chatManager.coloredRawMessage("&c&l✘ &cArena validation failed! Please configure following spawn properly: " + loc + " (cannot be world spawn location)"));
					return;
				}
			}

			ArenaRegistry.unregisterArena(arena);

			Arena newArena = new Arena(arena.getId());

			newArena.setReady(true);
			newArena.setStartLocation(LocationSerializer.fromString(config.getString(path + "startLocation")));
			newArena.setEndLocation(LocationSerializer.fromString(config.getString(path + "endLocation")));
			newArena.start();

			player.sendMessage(chatManager.coloredRawMessage("&a&l✔ &aValidation succeeded! Registering new arena instance: " + newArena.getId()));

			config.set(path + "ready", true);
			ConfigUtils.saveConfig(plugin, config, "arenas");

			ArenaRegistry.registerArena(newArena);
		}), 8, 2);
	}
}