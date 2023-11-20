package me.despical.whackme.api;

import me.despical.commons.configuration.ConfigUtils;
import me.despical.commons.sorter.SortUtils;
import me.despical.whackme.WhackMe;
import me.despical.whackme.user.data.MysqlManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
		if (plugin.getUserManager().getUserDatabase() instanceof MysqlManager) {
			MysqlManager mysqlManager = (MysqlManager) plugin.getUserManager().getUserDatabase();

			try (final Connection connection = plugin.getMysqlDatabase().getConnection()) {
				final Statement statement = connection.createStatement();
				final ResultSet set = statement.executeQuery(String.format("SELECT UUID, %s FROM %s ORDER BY %s", stat.getName(), mysqlManager.getTable(), stat.getName()));
				final Map<UUID, Integer> column = new LinkedHashMap<>();

				while (set.next()) {
					column.put(UUID.fromString(set.getString("UUID")), set.getInt(stat.getName()));
				}

				return column;
			} catch (SQLException e) {
				plugin.getLogger().warning("SQLException occurred during getting statistics from database!");
				return new LinkedHashMap<>();
			}
		}

		final FileConfiguration config = ConfigUtils.getConfig(plugin, "stats");
		final Map<UUID, Integer> stats = new LinkedHashMap<UUID, Integer>();

		for (String string : config.getKeys(false)) {
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