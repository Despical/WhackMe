package me.despical.whackme.events;

import me.despical.whackme.WhackMe;
import me.despical.whackme.handlers.ChatManager;
import org.bukkit.event.Listener;

/**
 * @author Despical
 * <p>
 * Created at 22.06.2022
 */
public abstract class ListenerAdapter implements Listener {

	protected final WhackMe plugin;
	protected final ChatManager chatManager;

	public ListenerAdapter(WhackMe plugin) {
		this.plugin = plugin;
		this.chatManager = plugin.getChatManager();
		this.plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	public static void registerEvents(WhackMe plugin) {
		new Events(plugin);
	}
}