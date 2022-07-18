package me.despical.whackme.api;

import me.despical.commons.configuration.ConfigUtils;
import me.despical.commons.sorter.SortUtils;
import me.despical.commons.util.LogUtils;
import me.despical.whackme.ConfigPreferences;
import me.despical.whackme.Main;
import me.despical.whackme.user.data.MysqlManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author Despical
 * <p>
 * Created at 20.06.2022
 */
public class StatsStorage {

	private static final Main plugin = JavaPlugin.getPlugin(Main.class);

	public static Map<UUID, Integer> getStats(StatisticType stat) {
		if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.DATABASE_ENABLED)) {
			try (Connection connection = plugin.getMysqlDatabase().getConnection()) {
				final Statement statement = connection.createStatement();
				final ResultSet set = statement.executeQuery("SELECT UUID, " + stat.name + " FROM " + ((MysqlManager) plugin.getUserManager().getDatabase()).getTableName() + " ORDER BY " + stat.name);
				final Map<UUID, Integer> column = new HashMap<>();

				while (set.next()) {
					column.put(UUID.fromString(set.getString("UUID")), set.getInt(stat.name));
				}

				return column;
			} catch (SQLException exception) {
				LogUtils.log("SQL Exception occurred! " + exception.getSQLState() + " (" + exception.getErrorCode() + ")");
				LogUtils.sendConsoleMessage("&cCould not get contents from MySQL database!");
				return null;
			}
		}

		FileConfiguration config = ConfigUtils.getConfig(plugin, "stats");
		Map<UUID, Integer> stats = config.getKeys(false).stream().collect(Collectors.toMap(UUID::fromString, string -> config.getInt(string + "." + stat.name), (a, b) -> b));

		return SortUtils.sortByValue(stats);
	}

	public static int getUserStats(Player player, StatisticType statisticType) {
		return plugin.getUserManager().getUser(player).getStat(statisticType);
	}

	public enum StatisticType {
		TOURS_PLAYED("toursplayed"), RECORD_SCORE("recordscore"), LOCAL_SCORE("local_score", false);

		String name;
		boolean persistent;

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