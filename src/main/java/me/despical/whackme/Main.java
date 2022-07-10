package me.despical.whackme;

import me.despical.commons.compat.VersionResolver;
import me.despical.commons.database.MysqlDatabase;
import me.despical.commons.exception.ExceptionLogHandler;
import me.despical.commons.miscellaneous.AttributeUtils;
import me.despical.commons.serializer.InventorySerializer;
import me.despical.commons.util.Collections;
import me.despical.commons.util.JavaVersion;
import me.despical.commons.util.LogUtils;
import me.despical.whackme.api.StatsStorage;
import me.despical.whackme.arena.Arena;
import me.despical.whackme.arena.ArenaRegistry;
import me.despical.whackme.command.CommandHandler;
import me.despical.whackme.event.Events;
import me.despical.whackme.handler.ChatManager;
import me.despical.whackme.handler.PlaceholderManager;
import me.despical.whackme.handler.SoundManager;
import me.despical.whackme.user.User;
import me.despical.whackme.user.UserManager;
import me.despical.whackme.user.data.MysqlManager;
import org.bukkit.entity.Player;
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
	private SoundManager soundManager;
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
			Player player = arena.getPlayer();
			
			if (player == null) continue;
			
			User user = userManager.getUser(player);
			int score = user.getStat(StatsStorage.StatisticType.LOCAL_SCORE);

			if (score > user.getStat(StatsStorage.StatisticType.RECORD_SCORE)) {
				user.setStat(StatsStorage.StatisticType.RECORD_SCORE, score);

				player.sendMessage(chatManager.message("in_game.finish_record_message").replace("%points%", Integer.toString(userManager.getUser(player).getStat(StatsStorage.StatisticType.LOCAL_SCORE))));
			} else {
				player.sendMessage(chatManager.message("in_game.finish_message").replace("%points%", Integer.toString(userManager.getUser(player).getStat(StatsStorage.StatisticType.LOCAL_SCORE))));
			}

			user.addStat(StatsStorage.StatisticType.TOURS_PLAYED, 1);

			userManager.getDatabase().saveAllStatistic(user);

			if (configPreferences.getOption(ConfigPreferences.Option.CLEAR_INVENTORY)) {
				player.getInventory().clear();
			}

			if (configPreferences.getOption(ConfigPreferences.Option.INVENTORY_MANAGER_ENABLED)) {
				InventorySerializer.loadInventory(this, player);
			}

			AttributeUtils.resetAttackCooldown(player);

			arena.getBossBarManager().removePlayer();
			arena.teleportToEndLocation();
			arena.cleanGameArea();
		}

		saveAllUserStatistics();

		LogUtils.disableLogging();
	}

	private void initClasses() {
		if (configPreferences.getOption(ConfigPreferences.Option.DATABASE_ENABLED)) {
			database = new MysqlDatabase(this, "mysql");
		}

		this.chatManager = new ChatManager(this);
		this.commandHandler = new CommandHandler(this);
		this.userManager = new UserManager(this);
		this.soundManager = new SoundManager(this);

		new Events(this);

		ArenaRegistry.registerArenas();

		registerSoftDependencies();
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

	private void registerSoftDependencies() {
		LogUtils.log("Hooking into soft dependencies.");

		if (chatManager.isPapiEnabled()) {
			LogUtils.log("Hooking into PlaceholderAPI.");
			new PlaceholderManager(this);
		}

		LogUtils.log("Hooked into soft dependencies.");
	}

	private void saveAllUserStatistics() {
		for (Player player : getServer().getOnlinePlayers()) {
			User user = userManager.getUser(player);

			if (userManager.getDatabase() instanceof MysqlManager) {
				StringBuilder update = new StringBuilder(" SET ");

				for (StatsStorage.StatisticType stat : StatsStorage.StatisticType.values()) {
					if (!stat.isPersistent()) continue;
					int val = user.getStat(stat);

					if (update.toString().equalsIgnoreCase(" SET ")) {
						update.append(stat.getName()).append("'='").append(val);
					}

					update.append(", ").append(stat.getName()).append("'='").append(val);
				}

				String finalUpdate = update.toString();
				MysqlManager database = ((MysqlManager) userManager.getDatabase());
				database.getDatabase().executeUpdate("UPDATE " + database.getTableName() + finalUpdate + " WHERE UUID='" + user.getUniqueId().toString() + "';");
				continue;
			}

			userManager.getDatabase().saveAllStatistic(user);
		}
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

	public SoundManager getSoundManager() {
		return soundManager;
	}

	public UserManager getUserManager() {
		return userManager;
	}
}