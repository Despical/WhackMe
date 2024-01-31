package me.despical.whackme.handlers;

import me.clip.placeholderapi.PlaceholderAPI;
import me.despical.commandframework.CommandFramework;
import me.despical.commons.configuration.ConfigUtils;
import me.despical.commons.util.Strings;
import me.despical.whackme.WhackMe;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * @author Despical
 * <p>
 * Created at 18.06.2022
 */
public class ChatManager {

	private final WhackMe plugin;
	private final String prefix;
	private final boolean papiEnabled;

	private FileConfiguration config;

	public ChatManager(WhackMe plugin) {
		this.plugin = plugin;
		this.config = ConfigUtils.getConfig(plugin, "messages");
		this.prefix = message("in_game.plugin_prefix");
		this.papiEnabled = plugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI");

		CommandFramework.NO_PERMISSION = message("Commands.No-Permission");
	}

	public boolean isPapiEnabled() {
		return papiEnabled;
	}

	public String coloredRawMessage(String message) {
		return Strings.format(message);
	}

	public String message(String path) {
		path = me.despical.commons.string.StringUtils.capitalize(path.replace('_', '-'), '-', '.');
		return coloredRawMessage(config.getString(path));
	}

	public String prefixedMessage(String path) {
		return prefix + message(path);
	}

	public String message(String path, Player player) {
		String returnString = message(path);
		returnString = formatPlaceholders(returnString, player);

		return returnString;
	}

	public String formatPlaceholders(String message, Player player) {
		String returnString = message;
		returnString = returnString.replace("%player%", player.getName());

		if (papiEnabled) {
			returnString = PlaceholderAPI.setPlaceholders(player, returnString);
		}

		return coloredRawMessage(returnString);
	}

	public List<String> getStringList(String path) {
		path = me.despical.commons.string.StringUtils.capitalize(path.replace('_', '-'), '-', '.');
		return config.getStringList(path);
	}

	public void reloadConfig() {
		config = ConfigUtils.getConfig(plugin, "messages");
	}
}