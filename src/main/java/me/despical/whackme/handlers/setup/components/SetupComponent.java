package me.despical.whackme.handlers.setup.components;

import me.despical.commons.configuration.ConfigUtils;
import me.despical.commons.serializer.LocationSerializer;
import me.despical.inventoryframework.pane.StaticPane;
import me.despical.whackme.WhackMe;
import me.despical.whackme.handlers.ChatManager;
import me.despical.whackme.handlers.setup.SetupInventory;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Set;

/**
 * @author Despical
 * <p>
 * Created at 20.06.2022
 */
public abstract class SetupComponent {

	protected SetupInventory setup;

	public SetupComponent(SetupInventory setup) {
		this.setup = setup;
	}

	public abstract void injectComponents(StaticPane pane);

	protected final String isOptionDoneBool(String path) {
		FileConfiguration config = ConfigUtils.getConfig(setup.getPlugin(), "arenas");
		return config.isSet(path) ? LocationSerializer.isDefaultLocation(config.getString(path)) ? "&c&l✘ Not Completed" : "&a&l✔ Completed" : "&c&l✘ Not Completed";
	}
}