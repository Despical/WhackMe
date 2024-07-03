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
		ItemBuilder registerItem;

		if (arena.isReady()) {
			registerItem = new ItemBuilder(XMaterial.BARRIER)
				.name("&a&l           Arena Registered")
				.lore("&7Good job, you went through whole setup!")
				.lore("&7      You can play on this arena now!")
				.enchantment(Enchantment.DURABILITY)
				.flag(ItemFlag.HIDE_ENCHANTS);
		} else {
			registerItem = new ItemBuilder(XMaterial.FIREWORK_ROCKET)
				.name("       &e&lFinish Arena Setup")
				.lore("&7  Click this when you are done.")
				.lore("&7You'll still be able to edit arena.");
		}

		pane.addItem(GuiItem.of(registerItem.build(), e -> {
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

			plugin.getSignManager().updateSign(arena);

			player.sendMessage(plugin.getChatManager().coloredRawMessage("&a&l✔ &aValidation succeeded! Registering new arena instance: " + arena.getId()));

			config.set(path + "ready", true);
			ConfigUtils.saveConfig(plugin, config, "arenas");
		}), 8, 2);
	}
}