package me.despical.whackme;

import me.despical.commandframework.CommandFramework;
import me.despical.commons.database.MysqlDatabase;
import me.despical.commons.serializer.InventorySerializer;
import me.despical.commons.util.Collections;
import me.despical.commons.util.UpdateChecker;
import me.despical.whackme.api.StatsStorage;
import me.despical.whackme.arena.Arena;
import me.despical.whackme.arena.ArenaRegistry;
import me.despical.whackme.arena.managers.ArenaManager;
import me.despical.whackme.commands.AdminCommands;
import me.despical.whackme.commands.PlayerCommands;
import me.despical.whackme.events.Events;
import me.despical.whackme.handlers.ChatManager;
import me.despical.whackme.handlers.PlaceholderManager;
import me.despical.whackme.handlers.ReloadManager;
import me.despical.whackme.handlers.SoundManager;
import me.despical.whackme.handlers.rewards.RewardsFactory;
import me.despical.whackme.handlers.sign.SignManager;
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
public class WhackMe extends JavaPlugin {

	private ChatManager chatManager;
	private CommandFramework commandFramework;
	private ConfigPreferences configPreferences;
	private MysqlDatabase database;
	private SoundManager soundManager;
	private UserManager userManager;
	private RewardsFactory rewardsFactory;
	private ArenaRegistry arenaRegistry;
	private SignManager signManager;
	private ArenaManager arenaManager;
	private ReloadManager reloadManager;

	@Override
	public void onEnable() {
		initializeClasses();
		checkUpdate();

		getLogger().info("Initialization finished. Consider donating: https://buymeacoffee.com/despical");
	}

	@Override
	public void onDisable() {
		for (Arena arena : arenaRegistry.getArenas()) {
			Player player = arena.getPlayer();
			
			if (player == null) continue;
			
			User user = userManager.getUser(player);
			user.addStat(StatsStorage.StatisticType.TOURS_PLAYED, 1);
			user.resetAttackCooldown();

			int score = user.getStat(StatsStorage.StatisticType.LOCAL_SCORE);

			if (score > user.getStat(StatsStorage.StatisticType.RECORD_SCORE)) {
				user.setStat(StatsStorage.StatisticType.RECORD_SCORE, score);

				player.sendMessage(chatManager.message("in_game.finish_record_message").replace("%points%", Integer.toString(user.getStat(StatsStorage.StatisticType.LOCAL_SCORE))));
			} else {
				player.sendMessage(chatManager.message("in_game.finish_message").replace("%points%", Integer.toString(user.getStat(StatsStorage.StatisticType.LOCAL_SCORE))));
			}

			if (getOption(ConfigPreferences.Option.CLEAR_INVENTORY)) player.getInventory().clear();
			if (getOption(ConfigPreferences.Option.INVENTORY_MANAGER_ENABLED)) InventorySerializer.loadInventory(this, player);

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
		this.signManager = new SignManager(this);
		this.arenaManager = new ArenaManager(this);
		this.reloadManager = new ReloadManager(this);

		if (getOption(ConfigPreferences.Option.DATABASE_ENABLED)) database = new MysqlDatabase(this, "mysql");
		if (chatManager.isPapiEnabled()) new PlaceholderManager(this);

		new Events(this);
		new PlayerCommands(this);
		new AdminCommands(this);

		Metrics metrics = new Metrics(this, 15722);
		metrics.addCustomChart(new SimplePie("database_enabled", () -> getOption(ConfigPreferences.Option.DATABASE_ENABLED) ? "Enabled" : "Disabled"));
		metrics.addCustomChart(new SimplePie("update_notifier", () -> getOption(ConfigPreferences.Option.UPDATE_NOTIFIER_ENABLED) ? "Enabled" : "Disabled"));
	}

	private void setupConfigurationFiles() {
		Collections.streamOf("config", "arenas", "stats", "mysql", "messages", "rewards").filter(name -> !new File(getDataFolder(),name + ".yml").exists()).forEach(name -> saveResource(name + ".yml", false));
	}

	private void checkUpdate() {
		if (!getOption(ConfigPreferences.Option.UPDATE_NOTIFIER_ENABLED)) return;

		UpdateChecker.init(this, 104912).requestUpdateCheck().whenComplete((result, exception) -> {
			if (result.requiresUpdate()) {
				final Logger logger = getLogger();

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

	@NotNull
	public ArenaRegistry getArenaRegistry() {
		return arenaRegistry;
	}

	@NotNull
	public SignManager getSignManager() {
		return signManager;
	}

	@NotNull
	public ArenaManager getArenaManager() {
		return arenaManager;
	}

	@NotNull
	public ReloadManager getReloadManager() {
		return reloadManager;
	}

	private void saveAllUserStatistics() {
		for (final Player player : getServer().getOnlinePlayers()) {
			final User user = userManager.getUser(player);

			if (userManager.getUserDatabase() instanceof MysqlManager) {
				final MysqlManager mysqlManager = (MysqlManager) userManager.getUserDatabase();
				final StringBuilder builder = new StringBuilder(" SET ");

				for (final StatsStorage.StatisticType stat : StatsStorage.StatisticType.values()) {
					if (!stat.isPersistent()) continue;

					final int value = user.getStat(stat);
					final String name = stat.getName();

					if (builder.toString().equalsIgnoreCase(" SET ")) {
						builder.append(name).append("=").append(value);
					}

					builder.append(", ").append(name).append("=").append(value);
				}

				final String update = builder.toString();

				mysqlManager.getDatabase().executeUpdate(String.format("UPDATE %s%s WHERE UUID='%s';", mysqlManager.getTable(), update, user.getUniqueId().toString()));
				continue;
			}

			userManager.getUserDatabase().saveStatistics(user);
		}
	}
}