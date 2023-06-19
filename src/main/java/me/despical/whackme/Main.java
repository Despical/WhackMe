package me.despical.whackme;

import me.despical.commandframework.CommandFramework;
import me.despical.commons.database.MysqlDatabase;
import me.despical.commons.miscellaneous.AttributeUtils;
import me.despical.commons.serializer.InventorySerializer;
import me.despical.commons.util.Collections;
import me.despical.commons.util.UpdateChecker;
import me.despical.whackme.api.StatsStorage;
import me.despical.whackme.arena.Arena;
import me.despical.whackme.arena.ArenaRegistry;
import me.despical.whackme.commands.AbstractCommand;
import me.despical.whackme.events.ListenerAdapter;
import me.despical.whackme.handlers.ChatManager;
import me.despical.whackme.handlers.PlaceholderManager;
import me.despical.whackme.handlers.SoundManager;
import me.despical.whackme.handlers.rewards.RewardsFactory;
import me.despical.whackme.user.User;
import me.despical.whackme.user.UserManager;
import me.despical.whackme.user.data.MysqlManager;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.logging.Logger;

/**
 * @author Despical
 * <p>
 * Created at 18.06.2022
 */
public class Main extends JavaPlugin {

	private ArenaRegistry arenaRegistry;
	private ChatManager chatManager;
	private CommandFramework commandFramework;
	private ConfigPreferences configPreferences;
	private MysqlDatabase database;
	private SoundManager soundManager;
	private UserManager userManager;
	private RewardsFactory rewardsFactory;

	@Override
	public void onEnable() {
		initializeClasses();
		checkUpdate();

		getLogger().info("Initialization finished. Join our Discord server: https://discord.gg/rVkaGmyszE");
	}

	@Override
	public void onDisable() {
		for (final Arena arena : arenaRegistry.getArenas()) {
			final Player player = arena.getPlayer();
			
			if (player == null) continue;
			
			final User user = userManager.getUser(player);
			user.addStat(StatsStorage.StatisticType.TOURS_PLAYED, 1);

			final int score = user.getStat(StatsStorage.StatisticType.LOCAL_SCORE);

			if (score > user.getStat(StatsStorage.StatisticType.RECORD_SCORE)) {
				user.setStat(StatsStorage.StatisticType.RECORD_SCORE, score);

				player.sendMessage(chatManager.message("in_game.finish_record_message").replace("%points%", Integer.toString(user.getStat(StatsStorage.StatisticType.LOCAL_SCORE))));
			} else {
				player.sendMessage(chatManager.message("in_game.finish_message").replace("%points%", Integer.toString(user.getStat(StatsStorage.StatisticType.LOCAL_SCORE))));
			}

			userManager.getDatabase().saveAllStatistic(user);

			if (configPreferences.getOption(ConfigPreferences.Option.CLEAR_INVENTORY)) player.getInventory().clear();
			if (configPreferences.getOption(ConfigPreferences.Option.INVENTORY_MANAGER_ENABLED)) InventorySerializer.loadInventory(this, player);

			AttributeUtils.resetAttackCooldown(player);

			arena.getBossBarManager().removePlayer();
			arena.teleportToEndLocation();
			arena.cleanGameArea();
		}

		saveAllUserStatistics();
	}

	private void initializeClasses() {
		this.setupConfigurationFiles();

		this.configPreferences = new ConfigPreferences(this);
		this.chatManager = new ChatManager(this);
		this.commandFramework = new CommandFramework(this);
		this.userManager = new UserManager(this);
		this.soundManager = new SoundManager(this);
		this.rewardsFactory = new RewardsFactory(this);
		this.arenaRegistry = new ArenaRegistry(this);

		if (configPreferences.getOption(ConfigPreferences.Option.DATABASE_ENABLED)) database = new MysqlDatabase(this, "mysql");
		if (chatManager.isPapiEnabled()) new PlaceholderManager(this);

		ListenerAdapter.registerEvents(this);
		AbstractCommand.registerCommands(this);

		final Metrics metrics = new Metrics(this, 15722);
		metrics.addCustomChart(new SimplePie("database_enabled", () -> configPreferences.getOption(ConfigPreferences.Option.DATABASE_ENABLED) ? "Enabled" : "Disabled"));
		metrics.addCustomChart(new SimplePie("update_notifier", () -> configPreferences.getOption(ConfigPreferences.Option.UPDATE_NOTIFIER_ENABLED) ? "Enabled" : "Disabled"));
	}

	private void setupConfigurationFiles() {
		Collections.streamOf("arenas", "stats", "mysql", "messages", "rewards").filter(name -> !new File(getDataFolder(),name + ".yml").exists()).forEach(name -> saveResource(name + ".yml", false));
	}

	private void checkUpdate() {
		if (!configPreferences.getOption(ConfigPreferences.Option.UPDATE_NOTIFIER_ENABLED)) return;

		UpdateChecker.init(this, 104912).requestUpdateCheck().whenComplete((result, exception) -> {
			if (result.requiresUpdate()) {
				final Logger logger = getLogger();

				logger.info("Found a new version available: v" + result.getNewestVersion());
				logger.info("Download it on SpigotMC:");
				logger.info("https://www.spigotmc.org/resources/whack-me.104912");
			}
		});
	}

	private void saveAllUserStatistics() {
		for (final Player player : getServer().getOnlinePlayers()) {
			final User user = userManager.getUser(player);

			if (userManager.getDatabase() instanceof MysqlManager) {
				final StringBuilder builder = new StringBuilder(" SET ");
				final MysqlManager database = ((MysqlManager) userManager.getDatabase());

				for (StatsStorage.StatisticType stat : StatsStorage.StatisticType.values()) {
					if (!stat.isPersistent()) continue;

					final int value = user.getStat(stat);
					final String name = stat.getName();

					if (builder.toString().equalsIgnoreCase(" SET ")) {
						builder.append(name).append("'='").append(value);
					}

					builder.append(", ").append(name).append("'='").append(value);
				}

				final String update = builder.toString();
				database.getDatabase().executeUpdate("UPDATE " + database.getTableName() + update + " WHERE UUID='" + user.getUniqueId().toString() + "';");
				continue;
			}

			userManager.getDatabase().saveAllStatistic(user);
		}
	}

	@NotNull
	public ArenaRegistry getArenaRegistry() {
		return arenaRegistry;
	}

	@NotNull
	public ChatManager getChatManager() {
		return chatManager;
	}

	@NotNull
	public CommandFramework getCommandFramework() {
		return commandFramework;
	}

	@NotNull
	public ConfigPreferences getConfigPreferences() {
		return configPreferences;
	}

	@NotNull
	public MysqlDatabase getMysqlDatabase() {
		return database;
	}

	@NotNull
	public SoundManager getSoundManager() {
		return soundManager;
	}

	@NotNull
	public UserManager getUserManager() {
		return userManager;
	}

	@NotNull
	public RewardsFactory getRewardsFactory() {
		return rewardsFactory;
	}
}