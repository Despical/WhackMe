package me.despical.whackme.api;

import me.despical.commons.configuration.ConfigUtils;
import me.despical.commons.sorter.SortUtils;
import me.despical.whackme.WhackMe;
import me.despical.whackme.user.data.MysqlManager;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Despical
 * <p>
 * Created at 20.06.2022
 */
public class StatsStorage {

	private static final WhackMe plugin = JavaPlugin.getPlugin(WhackMe.class);

	@NotNull
	@Contract("null -> fail")
	public static Map<UUID, Integer> getStats(StatisticType stat) {
		if (plugin.getUserManager().getUserDatabase() instanceof MysqlManager mysqlManager) {
			try (final var connection = plugin.getMysqlDatabase().getConnection()) {
				final var statement = connection.createStatement();
				final var set = statement.executeQuery("SELECT UUID, %s FROM %s ORDER BY %s".formatted(stat.getName(), mysqlManager.getTable(), stat.getName()));
				final var column = new LinkedHashMap<UUID, Integer>();

				while (set.next()) {
					column.put(UUID.fromString(set.getString("UUID")), set.getInt(stat.getName()));
				}

				return column;
			} catch (SQLException e) {
				plugin.getLogger().warning("SQLException occurred during getting statistics from database!");
				return new LinkedHashMap<>();
			}
		}

		final var config = ConfigUtils.getConfig(plugin, "stats");
		final var stats = new LinkedHashMap<UUID, Integer>();

		for (var string : config.getKeys(false)) {
			stats.put(UUID.fromString(string), config.getInt(string + "." + stat.getName()));
		}

		return SortUtils.sortByValue(stats);
	}

	public static int getUserStats(Player player, StatisticType statisticType) {
		return plugin.getUserManager().getUser(player).getStat(statisticType);
	}

	public enum StatisticType {

		TOURS_PLAYED("toursplayed"),
		RECORD_SCORE("recordscore"),
		LOCAL_SCORE("local_score", false);

		final String name;
		final boolean persistent;

		StatisticType(String name) {
			this (name, true);
		}

		StatisticType(String name, boolean persistent) {
			this.name = name;
			this.persistent = persistent;
		}

		public String getName() {
			return name;
		}

		public boolean isPersistent() {
			return persistent;
		}
	}
}