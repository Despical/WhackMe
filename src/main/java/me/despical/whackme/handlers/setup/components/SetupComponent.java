package me.despical.whackme.handlers.setup.components;

import me.despical.commons.configuration.ConfigUtils;
import me.despical.inventoryframework.pane.StaticPane;
import me.despical.whackme.Main;
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

	Main plugin = JavaPlugin.getPlugin(Main.class);
	ChatManager chatManager = plugin.getChatManager();
	FileConfiguration config = ConfigUtils.getConfig(plugin, "arenas");

	void injectComponents(SetupInventory setupInventory, StaticPane pane);
}