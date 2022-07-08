package me.despical.whackme.event;

import me.despical.whackme.Main;
import org.bukkit.event.Listener;

/**
 * @author Despical
 * <p>
 * Created at 22.06.2022
 */
public abstract class ListenerAdapter implements Listener {

	protected final Main plugin;

	public ListenerAdapter(Main plugin) {
		this.plugin = plugin;
		this.plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
}