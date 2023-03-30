package me.despical.whackme.handlers.setup;

import me.despical.commons.configuration.ConfigUtils;
import me.despical.commons.serializer.LocationSerializer;
import me.despical.whackme.Main;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * @author Despical
 * <p>
 * Created at 20.06.2022
 */
public class SetupUtilities {

	private final FileConfiguration config;

	SetupUtilities(Main plugin) {
		this.config = ConfigUtils.getConfig(plugin, "arenas");
	}

	public String isOptionDoneBool(String path) {
		return config.isSet(path) ? LocationSerializer.isDefaultLocation(config.getString(path)) ? "&c&l✘ Not Completed" : "&a&l✔ Completed" : "&c&l✘ Not Completed";
	}
}