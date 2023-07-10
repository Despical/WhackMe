package me.despical.whackme;

import me.despical.commons.compat.XMaterial;
import me.despical.commons.item.ItemBuilder;
import me.despical.commons.item.ItemUtils;
import me.despical.commons.serializer.InventorySerializer;
import me.despical.commons.string.StringUtils;
import me.despical.commons.util.function.DoubleSupplier;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * @author Despical
 * <p>
 * Created at 20.06.2022
 */
public class ConfigPreferences {

	public static ItemStack RED_BLOCK, GREEN_BLOCK, CYAN_BLOCK;
	private static final Main plugin = JavaPlugin.getPlugin(Main.class);

	private final Map<Option, Boolean> options;

	private double pointBlockMultiplier;
	private long ticks;
	private boolean isAsync;

	public ConfigPreferences() {
		this.options = new HashMap<>();

		plugin.saveDefaultConfig();

		this.initializeItems();
		this.loadOptions();
	}

	public void reload() {
		this.loadOptions();
		this.initializeItems();
	}

	public double getPointBlockMultiplier() {
		return pointBlockMultiplier;
	}

	public long getTicks() {
		return ticks;
	}

	public boolean getOption(Option option) {
		return options.get(option);
	}

	private void loadOptions() {
		this.options.clear();

		final var config = plugin.getConfig();

		for (final var option : Option.values()) {
			options.put(option, config.getBoolean(option.path, option.def));
		}

		this.pointBlockMultiplier = Math.min(config.getDouble("Point-Blocks.Y-Multiplier"), .64);
		this.ticks = config.getLong("Point-Blocks.Ticks", 8);
		this.isAsync = config.getBoolean("Point-Blocks.Run-Async");
	}

	public boolean isAsync() {
		return isAsync;
	}

	public enum Option {

		BLOCK_COMMANDS(false), BOSS_BAR_ENABLED, CHAT_FORMAT_ENABLED, CLEAR_EFFECTS,
		CLEAR_INVENTORY, DATABASE_ENABLED(false), BLOCK_LEAVE_COMMAND(false),
		UPDATE_NOTIFIER_ENABLED, INVENTORY_MANAGER_ENABLED((config) -> {
			final var list = config.getStringList("Inventory-Manager.Do-Not-Restore");
			list.forEach(InventorySerializer::addNonSerializableElements);

			return config.getBoolean("Inventory-Manager.Enabled");
		});

		final String path;
		final boolean def;

		Option() {
			this (true);
		}

		Option(boolean def) {
			this.def = def;
			this.path = StringUtils.capitalize(name().replace('_', '-').toLowerCase(Locale.ENGLISH), '-', '.');
		}

		Option(DoubleSupplier<FileConfiguration, Boolean> supplier) {
			this.path = "";
			this.def = supplier.accept(plugin.getConfig());
		}
	}

	private void initializeItems() {
		final var config = plugin.getConfig();
		final var greenBlockMsg = config.getString("Point-Blocks.Punch-Me");
		final var redBlockMsg = config.getString("Point-Blocks.Dont-Punch-Me");
		final var cyanBlockMsg = config.getString("Point-Blocks.Ouch");


		GREEN_BLOCK = greenBlockMsg.startsWith("skull:") ? ItemUtils.getSkull(greenBlockMsg.substring(6)) : XMaterial.valueOf(greenBlockMsg).parseItem();
		RED_BLOCK = redBlockMsg.startsWith("skull:") ? ItemUtils.getSkull(redBlockMsg.substring(6)) : XMaterial.valueOf(redBlockMsg).parseItem();
		CYAN_BLOCK = cyanBlockMsg.startsWith("skull:") ? ItemUtils.getSkull(cyanBlockMsg.substring(6)) : XMaterial.valueOf(cyanBlockMsg).parseItem();

		GREEN_BLOCK = new ItemBuilder(GREEN_BLOCK).lore("greenBlock").build();
		RED_BLOCK = new ItemBuilder(RED_BLOCK).lore("redBlock").build();
		CYAN_BLOCK = new ItemBuilder(CYAN_BLOCK).lore("cyanBlock").build();
	}
}