package me.despical.whackme.commands;

import me.despical.whackme.WhackMe;
import me.despical.whackme.handlers.ChatManager;

public abstract class AbstractCommand {

	protected final WhackMe plugin;
	protected final ChatManager chatManager;

	public AbstractCommand(final WhackMe plugin) {
		this.plugin = plugin;
		this.chatManager = plugin.getChatManager();
		this.plugin.getCommandFramework().registerCommands(this);
	}

	public static void registerCommands(final WhackMe plugin) {
		final Class<?>[] commandClasses = new Class[] {PlayerCommands.class, AdminCommands.class};

		for (final var clazz : commandClasses) {
			try {
				clazz.getConstructor(WhackMe.class).newInstance(plugin);
			} catch (Exception exception) {
				exception.printStackTrace();
			}
		}
	}
}