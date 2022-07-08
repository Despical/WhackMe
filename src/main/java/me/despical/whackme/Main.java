package me.despical.whackme;

import me.despical.commons.compat.VersionResolver;
import me.despical.commons.database.MysqlDatabase;
import me.despical.commons.exception.ExceptionLogHandler;
import me.despical.commons.scoreboard.ScoreboardLib;
import me.despical.commons.util.Collections;
import me.despical.commons.util.JavaVersion;
import me.despical.commons.util.LogUtils;
import me.despical.whackme.arena.Arena;
import me.despical.whackme.arena.ArenaRegistry;
import me.despical.whackme.command.CommandHandler;
import me.despical.whackme.event.Events;
import me.despical.whackme.handler.ChatManager;
import me.despical.whackme.user.UserManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

/**
 * @author Despical
 * <p>
 * Created at 18.06.2022
 */
public class Main extends JavaPlugin {

	private boolean forceDisable;

	private ChatManager chatManager;
	private CommandHandler commandHandler;
	private ConfigPreferences configPreferences;
	private ExceptionLogHandler exceptionLogHandler;
	private MysqlDatabase database;
	private UserManager userManager;

	@Override
	public void onEnable() {
		this.configPreferences = new ConfigPreferences(this);

		if (forceDisable = !validateIfPluginShouldStart()) {
			getServer().getPluginManager().disablePlugin(this);
			return;
		}

		if (configPreferences.getOption(ConfigPreferences.Option.DEBUG_MODE)) {
			LogUtils.setLoggerName("WhackMe");
			LogUtils.enableLogging();

			getServer().getLogger().setParent(LogUtils.getLogger());
		}

		exceptionLogHandler = new ExceptionLogHandler(this);
		exceptionLogHandler.setMainPackage("me.despical");
		exceptionLogHandler.addBlacklistedClass("me.despical.commons.database.MysqlDatabase");
		exceptionLogHandler.setRecordMessage("[WhackMe] We have found a problem in the code, create an issue on GitHub!");

		setupFiles();
		initClasses();
	}

	@Override
	public void onDisable() {
		if (forceDisable) return;

		getLogger().removeHandler(exceptionLogHandler);

		for (Arena arena : ArenaRegistry.getArenas()) {
			arena.removePlayer();
		}

		LogUtils.disableLogging();
	}

	private void initClasses() {
		ScoreboardLib.setPluginInstance(this);

		if (configPreferences.getOption(ConfigPreferences.Option.DATABASE_ENABLED)) {
			database = new MysqlDatabase(this, "mysql");
		}

		this.chatManager = new ChatManager(this);
		this.commandHandler = new CommandHandler(this);
		this.userManager = new UserManager(this);

		new Events(this);

		ArenaRegistry.registerArenas();
	}

	private void setupFiles() {
		Collections.streamOf("arenas", "stats", "mysql", "messages").filter(name -> !new File(getDataFolder(),name + ".yml").exists()).forEach(name -> saveResource(name + ".yml", false));
	}

	private boolean validateIfPluginShouldStart() {
		if (!VersionResolver.isCurrentBetween(VersionResolver.ServerVersion.v1_9_R1, VersionResolver.ServerVersion.v1_19_R1)) {
			LogUtils.sendConsoleMessage("&cYour server version is not supported by Whack Me!");
			LogUtils.sendConsoleMessage("&cSadly, we must shut off. Maybe you consider changing your server version?");
			return false;
		}

		if (!configPreferences.getOption(ConfigPreferences.Option.IGNORE_WARNING_MESSAGES) && JavaVersion.getCurrentVersion().isAt(JavaVersion.JAVA_8)) {
			LogUtils.sendConsoleMessage("[WhackMe] &cThis plugin won't support Java 8 in future updates.");
			LogUtils.sendConsoleMessage("[WhackMe] &cSo, maybe consider to update your version, right?");
		}

		try {
			Class.forName("org.spigotmc.SpigotConfig");
		} catch (Exception e) {
			LogUtils.sendConsoleMessage("&cYour server software is not supported by Whack Me!");
			LogUtils.sendConsoleMessage("&cWe support only Spigot and its forks! Shutting off...");
			return false;
		}

		return true;
	}

	public ChatManager getChatManager() {
		return chatManager;
	}

	public CommandHandler getCommandHandler() {
		return commandHandler;
	}

	public ConfigPreferences getConfigPreferences() {
		return configPreferences;
	}

	public MysqlDatabase getMysqlDatabase() {
		return database;
	}

	public UserManager getUserManager() {
		return userManager;
	}
}