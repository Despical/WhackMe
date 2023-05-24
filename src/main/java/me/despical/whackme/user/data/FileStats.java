package me.despical.whackme.user.data;

import me.despical.commons.configuration.ConfigUtils;
import me.despical.whackme.api.StatsStorage;
import me.despical.whackme.user.User;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * @author Despical
 * <p>
 * Created at 20.06.2022
 */
public class FileStats implements UserDatabase {

	private final FileConfiguration config;

	public FileStats() {
		this.config = ConfigUtils.getConfig(plugin, "stats");
	}

	@Override
	public void saveStatistic(User user, StatsStorage.StatisticType stat) {
		config.set(user.getUniqueId().toString() + "." + stat.getName(), user.getStat(stat));

		ConfigUtils.saveConfig(plugin, config, "stats");
	}

	@Override
	public void saveAllStatistic(User user) {
		final String uuid = user.getUniqueId().toString();

		for (final StatsStorage.StatisticType stat : StatsStorage.StatisticType.values()) {
			if (!stat.isPersistent()) continue;

			config.set(uuid + "." + stat.getName(), user.getStat(stat));
		}

		ConfigUtils.saveConfig(plugin, config, "stats");
	}

	@Override
	public void loadStatistics(User user) {
		final String uuid = user.getUniqueId().toString();

		for (final StatsStorage.StatisticType stat : StatsStorage.StatisticType.values()) {
			user.setStat(stat, config.getInt(uuid + "." + stat.getName()));
		}
	}
}