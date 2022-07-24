package me.despical.whackme;

import me.despical.commons.compat.VersionResolver;
import me.despical.commons.database.MysqlDatabase;
import me.despical.commons.exception.ExceptionLogHandler;
import me.despical.commons.miscellaneous.AttributeUtils;
import me.despical.commons.serializer.InventorySerializer;
import me.despical.commons.util.Collections;
import me.despical.commons.util.JavaVersion;
import me.despical.commons.util.LogUtils;
import me.despical.commons.util.UpdateChecker;
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
import org.bstats.bukkit.Metrics;
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
		}

		exceptionLogHandler = new ExceptionLogHandler(this);
		exceptionLogHandler.setMainPackage("me.despical");
		exceptionLogHandler.addBlacklistedClass("me.despical.whackme.user.data.MysqlManager", "me.despical.commons.database.MysqlDatabase");
		exceptionLogHandler.setRecordMessage("[WhacKMe] We have found a bug in the code. Use our issue tracker on our GitHub repo with the following error given above or you can join our Discord server (https://discord.gg/rVkaGmyszE)");

		LogUtils.log("Initialization started!");
		long start = System.currentTimeMillis();

		setupFiles();
		initClasses();

		LogUtils.log("Initialization finished took {0} ms.", System.currentTimeMillis() - start);
	}

	@Override
	public void onDisable() {
		if (forceDisable) return;

		LogUtils.log("System disable initialized.");
		long start = System.currentTimeMillis();

		getServer().getLogger().removeHandler(exceptionLogHandler);

		for (Arena arena : ArenaRegistry.getArenas()) {
			Player player = arena.getPlayer();
			
			if (player == null) continue;
			
			User user = userManager.getUser(player);
			user.addStat(StatsStorage.StatisticType.TOURS_PLAYED, 1);

			int score = user.getStat(StatsStorage.StatisticType.LOCAL_SCORE);

			if (score > user.getStat(StatsStorage.StatisticType.RECORD_SCORE)) {
				user.setStat(StatsStorage.StatisticType.RECORD_SCORE, score);

				player.sendMessage(chatManager.message("in_game.finish_record_message").replace("%points%", Integer.toString(user.getStat(StatsStorage.StatisticType.LOCAL_SCORE))));
			} else {
				player.sendMessage(chatManager.message("in_game.finish_message").replace("%points%", Integer.toString(user.getStat(StatsStorage.StatisticType.LOCAL_SCORE))));
			}

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

		LogUtils.log("System disable finished took {0} ms.", System.currentTimeMillis() - start);
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

	private void checkUpdate() {
		if (!configPreferences.getOption(ConfigPreferences.Option.UPDATE_NOTIFIER_ENABLED)) return;

		UpdateChecker.init(this, 103482).requestUpdateCheck().whenComplete((result, exception) -> {
			if (result.requiresUpdate()) {
				LogUtils.sendConsoleMessage("[WhackMe] Found a new version available: v" + result.getNewestVersion());
				LogUtils.sendConsoleMessage("[WhackMe] Download it on SpigotMC:");
				LogUtils.sendConsoleMessage("[WhackMe] https://www.spigotmc.org/resources/whack-me-1-9-1-19.103482/");
			}
		});
	}

	private void registerSoftDependencies() {
		LogUtils.log("Hooking into soft dependencies.");

		startPluginMetrics();

		if (chatManager.isPapiEnabled()) {
			LogUtils.log("Hooking into PlaceholderAPI.");
			new PlaceholderManager(this);
		}

		LogUtils.log("Hooked into soft dependencies.");
	}

	private void startPluginMetrics() {
		final Metrics metrics = new Metrics(this, 15722);

		if (!metrics.isEnabled()) return;

		metrics.addCustomChart(new Metrics.SimplePie("database_enabled", () -> configPreferences.getOption(ConfigPreferences.Option.DATABASE_ENABLED) ? "Enabled" : "Disabled"));
		metrics.addCustomChart(new Metrics.SimplePie("update_notifier", () -> configPreferences.getOption(ConfigPreferences.Option.UPDATE_NOTIFIER_ENABLED) ? "Enabled" : "Disabled"));
	}

	private void saveAllUserStatistics() {
		for (Player player : getServer().getOnlinePlayers()) {
			final User user = userManager.getUser(player);

			if (userManager.getDatabase() instanceof MysqlManager) {
				final StringBuilder update = new StringBuilder(" SET ");

				for (StatsStorage.StatisticType stat : StatsStorage.StatisticType.values()) {
					if (!stat.isPersistent()) continue;

					final int val = user.getStat(stat);

					if (update.toString().equalsIgnoreCase(" SET ")) {
						update.append(stat.getName()).append("'='").append(val);
					}

					update.append(", ").append(stat.getName()).append("'='").append(val);
				}

				final String finalUpdate = update.toString();
				final MysqlManager database = ((MysqlManager) userManager.getDatabase());
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