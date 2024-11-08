package me.despical.whackme.user.data;

import me.despical.commons.configuration.ConfigUtils;
import me.despical.whackme.api.statistics.StatisticType;
import me.despical.whackme.user.User;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

/**
 * @author Despical
 * <p>
 * Created at 20.06.2022
 */
public class FileStatistics extends AbstractDatabase {

	private final FileConfiguration config;

	public FileStatistics() {
		this.config = ConfigUtils.getConfig(plugin, "stats");
	}

	@Override
	public void saveStatistic(@NotNull User user, StatisticType statisticType) {
		config.set(user.getUniqueId().toString() + "." + statisticType.getName(), user.getStat(statisticType));

		ConfigUtils.saveConfig(plugin, config, "stats");
	}

	@Override
	public void saveStatistics(@NotNull User user) {
		String uuid = user.getUniqueId().toString();

		for (StatisticType stat : StatisticType.values()) {
			if (stat.isPersistent()) {
				config.set(uuid + "." + stat.getName(), user.getStat(stat));
			}
		}

		ConfigUtils.saveConfig(plugin, config, "stats");
	}

	@Override
	public void saveAllStatistics() {
		for (User user : plugin.getUserManager().getUsers()) {
			String uuid = user.getUniqueId().toString();

			for (StatisticType stat : StatisticType.values()) {
				if (stat.isPersistent()) {
					config.set(uuid + "." + stat.getName(), user.getStat(stat));
				}
			}
		}

		ConfigUtils.saveConfig(plugin, config, "stats");
	}

	@Override
	public void loadStatistics(@NotNull User user) {
		String uuid = user.getUniqueId().toString();

		for (StatisticType stat : StatisticType.values()) {
			user.setStat(stat, config.getInt(uuid + "." + stat.getName()));
		}
	}

	@Override
	public void shutdown() {
		this.saveAllStatistics();
	}
}