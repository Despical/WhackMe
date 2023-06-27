package me.despical.whackme.events;

import me.despical.whackme.Main;
import me.despical.whackme.handlers.ChatManager;
import org.bukkit.event.Listener;

/**
 * @author Despical
 * <p>
 * Created at 22.06.2022
 */
public abstract class ListenerAdapter implements Listener {

	protected final Main plugin;
	protected final ChatManager chatManager;

	public ListenerAdapter(Main plugin) {
		this.plugin = plugin;
		this.chatManager = plugin.getChatManager();
		this.plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	public static void registerEvents(Main plugin) {
		final Class<?>[] listenerAdapters = {Events.class};

		try {
			for (final var listenerAdapter : listenerAdapters) {
				listenerAdapter.getConstructor(Main.class).newInstance(plugin);
			}
		} catch (Exception ignored) {
			plugin.getLogger().log(java.util.logging.Level.SEVERE, "An exception occurred on event registering.");
		}
	}
}