package me.despical.whackme;

import me.despical.commons.string.StringUtils;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * @author Despical
 * <p>
 * Created at 20.06.2022
 */
public class ConfigPreferences {

	private final Map<Option, Boolean> options;

	public ConfigPreferences(Main plugin) {
		this.options = new HashMap<>();

		plugin.saveDefaultConfig();

		for (Option option : Option.values()) {
			options.put(option, plugin.getConfig().getBoolean(option.path, option.def));
		}
	}

	public boolean getOption(Option option) {
		return options.get(option);
	}

	public enum Option {
		BLOCK_COMMANDS(false), BOSS_BAR_ENABLED, CHAT_FORMAT_ENABLED, CLEAR_EFFECTS, CLEAR_INVENTORY,
		DATABASE_ENABLED(false), IGNORE_WARNING_MESSAGES(false), INVENTORY_MANAGER_ENABLED,
		REWARDS_ENABLED(false),	UPDATE_NOTIFIER_ENABLED(false), SEND_SETUP_TIPS, DEBUG_MODE(false);

		String path;
		boolean def;

		Option() {
			this (true);
		}

		Option(boolean def) {
			this.def = def;
			this.path = StringUtils.capitalize(name().replace('_', '-').toLowerCase(Locale.ENGLISH), '-', '.');
		}
	}
}