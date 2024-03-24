package me.despical.whackme.handlers.setup.components;

import me.despical.commons.compat.XMaterial;
import me.despical.commons.configuration.ConfigUtils;
import me.despical.commons.item.ItemBuilder;
import me.despical.commons.serializer.LocationSerializer;
import me.despical.inventoryframework.GuiItem;
import me.despical.inventoryframework.pane.StaticPane;
import me.despical.whackme.WhackMe;
import me.despical.whackme.arena.Arena;
import me.despical.whackme.handlers.setup.SetupInventory;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;

import java.util.stream.Collectors;

/**
 * @author Despical
 * <p>
 * Created at 21.06.2022
 */
public class ArenaRegisterComponent extends SetupComponent {

	public ArenaRegisterComponent(SetupInventory setup) {
		super(setup);
	}

	@Override
	public void injectComponents(StaticPane pane) {
		WhackMe plugin = setup.getPlugin();
		Player player = setup.getPlayer();
		Arena arena = setup.getArena();
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
			final String path = String.format("instances.%s.", arena.getId());
			final FileConfiguration config = ConfigUtils.getConfig(plugin, "arenas");

			setup.closeInventory();

			if (config.getBoolean(path + "ready")) {
				player.sendMessage(plugin.getChatManager().coloredRawMessage("&a&l✔ &aThis arena was already validated and is ready to use!"));
				return;
			}

			final String[] locations = {"startLocation", "endLocation"};

			for (final String loc : locations) {
				if (!config.isSet(path + loc) || LocationSerializer.isDefaultLocation(config.getString(path + loc))) {
					player.sendMessage(plugin.getChatManager().coloredRawMessage("&c&l✘ &cArena validation failed! Please configure following spawn properly: " + loc + " (cannot be world spawn location)"));
					return;
				}
			}

			arena.setReady(true);
			arena.setStartLocation(LocationSerializer.fromString(config.getString(path + "startLocation")));
			arena.setEndLocation(LocationSerializer.fromString(config.getString(path + "endLocation")));
			arena.setLocations(config.getStringList(path + "portalLocations").stream().map(LocationSerializer::fromString).collect(Collectors.toList()));
			arena.start();

			player.sendMessage(plugin.getChatManager().coloredRawMessage("&a&l✔ &aValidation succeeded! Registering new arena instance: " + arena.getId()));

			config.set(path + "ready", true);
			ConfigUtils.saveConfig(plugin, config, "arenas");
		}), 8, 2);
	}
}