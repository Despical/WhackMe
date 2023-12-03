package me.despical.whackme.handlers.setup.components;

import me.despical.commons.configuration.ConfigUtils;
import me.despical.commons.serializer.LocationSerializer;
import me.despical.inventoryframework.pane.StaticPane;
import me.despical.whackme.WhackMe;
import me.despical.whackme.handlers.ChatManager;
import me.despical.whackme.handlers.setup.SetupInventory;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * @author Despical
 * <p>
 * Created at 20.06.2022
 */
public interface SetupComponent {

	WhackMe plugin = JavaPlugin.getPlugin(WhackMe.class);
	ChatManager chatManager = plugin.getChatManager();

	void injectComponents(SetupInventory setupInventory, StaticPane pane);

	default String isOptionDoneBool(String path) {
		FileConfiguration config = ConfigUtils.getConfig(plugin, "arenas");
		return config.isSet(path) ? LocationSerializer.isDefaultLocation(config.getString(path)) ? "&c&l✘ Not Completed" : "&a&l✔ Completed" : "&c&l✘ Not Completed";
	}
}