package me.despical.whackme.commands;

import me.despical.commons.configuration.ConfigUtils;
import me.despical.whackme.Main;
import me.despical.whackme.handlers.ChatManager;
import org.bukkit.configuration.file.FileConfiguration;

public abstract class AbstractCommand {

	protected final Main plugin;
	protected final ChatManager chatManager;
	protected final FileConfiguration arenaConfig;

	public AbstractCommand(final Main plugin) {
		this.plugin = plugin;
		this.chatManager = plugin.getChatManager();
		this.arenaConfig = ConfigUtils.getConfig(plugin, "arenas");
		this.plugin.getCommandFramework().registerCommands(this);
	}

	public static void registerCommands(final Main plugin) {
		final Class<?>[] commandClasses = new Class[] {AdminCommands.class, PlayerCommands.class, TabCompleter.class};

		for (final var clazz : commandClasses) {
			try {
				clazz.getConstructor(Main.class).newInstance(plugin);
			} catch (Exception exception) {
				exception.printStackTrace();
			}
		}
	}
}