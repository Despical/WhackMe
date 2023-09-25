package me.despical.whackme;

import me.despical.commandframework.CommandFramework;
import me.despical.commons.database.MysqlDatabase;
import me.despical.commons.miscellaneous.AttributeUtils;
import me.despical.commons.serializer.InventorySerializer;
import me.despical.commons.util.Collections;
import me.despical.commons.util.UpdateChecker;
import me.despical.whackme.api.StatsStorage;
import me.despical.whackme.arena.ArenaRegistry;
import me.despical.whackme.commands.AbstractCommand;
import me.despical.whackme.events.ListenerAdapter;
import me.despical.whackme.handlers.ChatManager;
import me.despical.whackme.handlers.PlaceholderManager;
import me.despical.whackme.handlers.SoundManager;
import me.despical.whackme.handlers.rewards.RewardsFactory;
import me.despical.whackme.user.UserManager;
import me.despical.whackme.user.data.MysqlManager;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * @author Despical
 * <p>
 * Created at 18.06.2022
 */
public class WhackMe extends JavaPlugin {

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

		getLogger().info("Initialization finished. Consider donating: https://buymeacoffee.com/despical");
	}

	@Override
	public void onDisable() {
		for (final var arena : arenaRegistry.getArenas()) {
			final var player = arena.getPlayer();
			
			if (player == null) continue;
			
			final var user = userManager.getUser(player);
			user.addStat(StatsStorage.StatisticType.TOURS_PLAYED, 1);

			final var score = user.getStat(StatsStorage.StatisticType.LOCAL_SCORE);

			if (score > user.getStat(StatsStorage.StatisticType.RECORD_SCORE)) {
				user.setStat(StatsStorage.StatisticType.RECORD_SCORE, score);

				player.sendMessage(chatManager.message("in_game.finish_record_message").replace("%points%", Integer.toString(user.getStat(StatsStorage.StatisticType.LOCAL_SCORE))));
			} else {
				player.sendMessage(chatManager.message("in_game.finish_message").replace("%points%", Integer.toString(user.getStat(StatsStorage.StatisticType.LOCAL_SCORE))));
			}

			userManager.getUserDatabase().saveStatistics(user);

			if (getOption(ConfigPreferences.Option.CLEAR_INVENTORY)) player.getInventory().clear();
			if (getOption(ConfigPreferences.Option.INVENTORY_MANAGER_ENABLED)) InventorySerializer.loadInventory(this, player);

			AttributeUtils.resetAttackCooldown(player);

			arena.getBossBarManager().removePlayer();
			arena.teleportToEndLocation();
			arena.cleanGameArea();
		}

		saveAllUserStatistics();
	}

	private void initializeClasses() {
		this.setupConfigurationFiles();

		this.configPreferences = new ConfigPreferences();
		this.chatManager = new ChatManager(this);
		this.commandFramework = new CommandFramework(this);
		this.userManager = new UserManager(this);
		this.soundManager = new SoundManager(this);
		this.rewardsFactory = new RewardsFactory(this);
		this.arenaRegistry = new ArenaRegistry(this);

		if (getOption(ConfigPreferences.Option.DATABASE_ENABLED)) database = new MysqlDatabase(this, "mysql");
		if (chatManager.isPapiEnabled()) new PlaceholderManager(this);

		ListenerAdapter.registerEvents(this);
		AbstractCommand.registerCommands(this);

		final Metrics metrics = new Metrics(this, 15722);
		metrics.addCustomChart(new SimplePie("database_enabled", () -> getOption(ConfigPreferences.Option.DATABASE_ENABLED) ? "Enabled" : "Disabled"));
		metrics.addCustomChart(new SimplePie("update_notifier", () -> getOption(ConfigPreferences.Option.UPDATE_NOTIFIER_ENABLED) ? "Enabled" : "Disabled"));
	}

	private void setupConfigurationFiles() {
		Collections.streamOf("arenas", "stats", "mysql", "messages", "rewards").filter(name -> !new File(getDataFolder(),name + ".yml").exists()).forEach(name -> saveResource(name + ".yml", false));
	}

	private void checkUpdate() {
		if (!getOption(ConfigPreferences.Option.UPDATE_NOTIFIER_ENABLED)) return;

		UpdateChecker.init(this, 104912).requestUpdateCheck().whenComplete((result, exception) -> {
			if (result.requiresUpdate()) {
				final var logger = getLogger();

				logger.info("Found a new version available: v" + result.getNewestVersion());
				logger.info("Download it on SpigotMC:");
				logger.info("https://spigotmc.org/resources/104912");
			}
		});
	}

	public boolean getOption(ConfigPreferences.Option option) {
		return configPreferences.getOption(option);
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

	@Deprecated
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

	public void reload() {
		configPreferences.reload();
		chatManager.reloadConfig();

		reloadConfig();
	}

	private void saveAllUserStatistics() {
		for (final var player : getServer().getOnlinePlayers()) {
			final var user = userManager.getUser(player);

			if (userManager.getUserDatabase() instanceof MysqlManager mysqlManager) {
				final var builder = new StringBuilder(" SET ");

				for (final var stat : StatsStorage.StatisticType.values()) {
					if (!stat.isPersistent()) continue;

					final var value = user.getStat(stat);
					final var name = stat.getName();

					if (builder.toString().equalsIgnoreCase(" SET ")) {
						builder.append(name).append("=").append(value);
					}

					builder.append(", ").append(name).append("=").append(value);
				}

				final var update = builder.toString();

				mysqlManager.getDatabase().executeUpdate("UPDATE %s%s WHERE UUID='%s';".formatted(mysqlManager.getTable(), update, user.getUniqueId().toString()));
				continue;
			}

			userManager.getUserDatabase().saveStatistics(user);
		}
	}
}