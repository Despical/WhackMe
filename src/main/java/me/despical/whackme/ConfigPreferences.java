package me.despical.whackme;

import me.despical.commons.compat.XMaterial;
import me.despical.commons.item.ItemBuilder;
import me.despical.commons.item.ItemUtils;
import me.despical.commons.string.StringUtils;
import org.bukkit.inventory.ItemStack;

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

	private final Main plugin;
	private final Map<Option, Boolean> options;

	private double pointBlockMultiplier;
	private boolean isAsync;

	public ConfigPreferences(Main plugin) {
		this.plugin = plugin;
		this.options = new HashMap<>();

		plugin.saveDefaultConfig();

		this.initializeItems(plugin);
		this.loadOptions();
	}

	public void reload() {
		this.loadOptions();
		this.initializeItems(plugin);
	}

	public double getPointBlockMultiplier() {
		return pointBlockMultiplier;
	}

	public boolean getOption(Option option) {
		return options.get(option);
	}

	private void loadOptions() {
		this.options.clear();

		for (final var option : Option.values()) {
			options.put(option, plugin.getConfig().getBoolean(option.path, option.def));
		}

		this.pointBlockMultiplier = Math.min(plugin.getConfig().getDouble("Point-Block-Y-Multiplier"), .64);
		this.isAsync = plugin.getConfig().getBoolean("Point-Blocks.Run-Async");
	}

	public boolean isAsync() {
		return isAsync;
	}

	public enum Option {

		BLOCK_COMMANDS(false), BOSS_BAR_ENABLED, CHAT_FORMAT_ENABLED, CLEAR_EFFECTS,
		CLEAR_INVENTORY, DATABASE_ENABLED(false), INVENTORY_MANAGER_ENABLED, BLOCK_LEAVE_COMMAND(false),
		UPDATE_NOTIFIER_ENABLED;

		final String path;
		final boolean def;

		Option() {
			this (true);
		}

		Option(boolean def) {
			this.def = def;
			this.path = StringUtils.capitalize(name().replace('_', '-').toLowerCase(Locale.ENGLISH), '-', '.');
		}
	}

	private void initializeItems(final Main plugin) {
		final var greenBlockMsg = plugin.getConfig().getString("Point-Blocks.Punch-Me");
		final var redBlockMsg = plugin.getConfig().getString("Point-Blocks.Dont-Punch-Me");
		final var cyanBlockMsg = plugin.getConfig().getString("Point-Blocks.Ouch");

		assert greenBlockMsg != null && redBlockMsg != null && cyanBlockMsg != null : "Something is null, hmm... (assertion failed)";

		GREEN_BLOCK = greenBlockMsg.startsWith("skull:") ? ItemUtils.getSkull(greenBlockMsg.substring(5)) : XMaterial.valueOf(greenBlockMsg).parseItem();
		RED_BLOCK = redBlockMsg.startsWith("skull:") ? ItemUtils.getSkull(redBlockMsg.substring(5)) : XMaterial.valueOf(redBlockMsg).parseItem();
		CYAN_BLOCK = cyanBlockMsg.startsWith("skull:") ? ItemUtils.getSkull(cyanBlockMsg.substring(5)) : XMaterial.valueOf(cyanBlockMsg).parseItem();

		GREEN_BLOCK = new ItemBuilder(GREEN_BLOCK).lore("greenBlock").build();
		RED_BLOCK = new ItemBuilder(RED_BLOCK).lore("redBlock").build();
		CYAN_BLOCK = new ItemBuilder(CYAN_BLOCK).lore("cyanBlock").build();
	}
}