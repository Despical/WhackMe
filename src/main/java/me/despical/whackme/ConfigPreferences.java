package me.despical.whackme;

import me.despical.commons.compat.XMaterial;
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

	private ItemStack redBlock, greenBlock, cyanBlock;

	private final Main plugin;
	private final double pointBlockMultiplier;
	private final Map<Option, Boolean> options;

	public ConfigPreferences(Main plugin) {
		this.plugin = plugin;
		this.options = new HashMap<>();

		plugin.saveDefaultConfig();

		this.initializeItems(plugin);
		this.pointBlockMultiplier = Math.min(plugin.getConfig().getDouble("Point-Block-Y-Multiplier"), .64);

		this.loadOptions();
	}

	public ItemStack getGreenBlock() {
		return greenBlock;
	}

	public ItemStack getRedBlock() {
		return redBlock;
	}

	public ItemStack getCyanBlock() {
		return cyanBlock;
	}

	public double getPointBlockMultiplier() {
		return pointBlockMultiplier;
	}

	public boolean getOption(Option option) {
		return options.get(option);
	}

	public void loadOptions() {
		this.options.clear();

		for (Option option : Option.values()) {
			options.put(option, plugin.getConfig().getBoolean(option.path, option.def));
		}
	}

	public enum Option {

		BLOCK_COMMANDS(false), BOSS_BAR_ENABLED, CHAT_FORMAT_ENABLED, CLEAR_EFFECTS,
		CLEAR_INVENTORY, DATABASE_ENABLED(false), INVENTORY_MANAGER_ENABLED, BLOCK_LEAVE_COMMAND(false),
		REWARDS_ENABLED(false), UPDATE_NOTIFIER_ENABLED, SEND_SETUP_TIPS, DEBUG_MODE(false);

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
		final String greenBlock = plugin.getConfig().getString("Point-Blocks.Punch-Me");
		final String redBlock = plugin.getConfig().getString("Point-Blocks.Dont-Punch-Me");
		final String cyanBlock = plugin.getConfig().getString("Point-Blocks.Ouch");

		if (!plugin.getConfig().getBoolean("Point-Blocks.Skulls-Enabled")) {
			this.greenBlock = XMaterial.valueOf(greenBlock).parseItem();
			this.redBlock = XMaterial.valueOf(redBlock).parseItem();
			this.cyanBlock = XMaterial.valueOf(cyanBlock).parseItem();
		} else {
			this.greenBlock = ItemUtils.getSkull(greenBlock);
			this.redBlock = ItemUtils.getSkull(redBlock);
			this.cyanBlock = ItemUtils.getSkull(cyanBlock);
		}
	}
}