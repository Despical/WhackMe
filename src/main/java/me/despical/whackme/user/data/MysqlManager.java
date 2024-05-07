package me.despical.whackme.user.data;

import me.despical.commons.configuration.ConfigUtils;
import me.despical.commons.database.MysqlDatabase;
import me.despical.whackme.WhackMe;
import me.despical.whackme.api.StatsStorage;
import me.despical.whackme.user.User;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author Despical
 * <p>
 * Created at 20.06.2022
 */
public class MysqlManager extends IUserDatabase {

	private final String table;

	private MysqlDatabase database;

	public MysqlManager(WhackMe plugin) {
		super(plugin);
		this.table = ConfigUtils.getConfig(plugin, "mysql").getString("table", "wm_stats");

		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
			this.database = plugin.getMysqlDatabase();

			try (final Connection connection = database.getConnection()) {
				final Statement statement = connection.createStatement();

				statement.executeUpdate(String.format(
						"CREATE TABLE IF NOT EXISTS `%s` (\n" +
						  "`UUID` char(36) NOT NULL PRIMARY KEY,\n" +
						  "`name` varchar(32) NOT NULL,\n" +
						  "`recordscore` int(11) NOT NULL DEFAULT '0',\n" +
						  "`toursplayed` int(11) NOT NULL DEFAULT '0',\n" +
						  "`whackedpluspointblocks` int(11) NOT NULL DEFAULT '0',\n" +
						  "`whackedminuspointblocks` int(11) NOT NULL DEFAULT '0',\n" +
						  "`longeststreak` int(11) NOT NULL DEFAULT '0');",
					table));
			} catch (SQLException exception) {
				exception.fillInStackTrace();

				plugin.getLogger().severe("Couldn't create statistics table on MySQL database!");
			}
		});
	}

	@Override
	public void saveStatistic(@NotNull User user, StatsStorage.StatisticType statisticType) {
		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> database.executeUpdate(String.format("UPDATE %s SET %s=%d WHERE UUID='%s';", table, statisticType.getName(), user.getStat(statisticType), user.getUniqueId().toString())));
	}

	@Override
	public void saveStatistics(@NotNull User user) {
		final StringBuilder builder = new StringBuilder(" SET ");

		for (final StatsStorage.StatisticType stat : StatsStorage.StatisticType.values()) {
			if (!stat.isPersistent()) continue;

			final String name = stat.getName();
			final int value = user.getStat(stat);

			if (builder.toString().equalsIgnoreCase(" SET ")) {
				builder.append(name).append("=").append(value);
			}

			builder.append(", ").append(name).append("=").append(value);
		}

		final String update = builder.toString();

		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> database.executeUpdate(String.format("UPDATE %s%s WHERE UUID='%s';", table, update, user.getUniqueId().toString())));
	}

	@Override
	public void loadStatistics(@NotNull User user) {
		final String uuid = user.getUniqueId().toString();

		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
			try (final Connection connection = database.getConnection()) {
				final Statement statement = connection.createStatement();
				final ResultSet result = statement.executeQuery(String.format("SELECT * from %s WHERE UUID='%s';", table, uuid));

				if (result.next()) {
					for (final StatsStorage.StatisticType stat : StatsStorage.StatisticType.values()) {
						if (!stat.isPersistent()) continue;

						user.setStat(stat, result.getInt(stat.getName()));
					}
				} else {
					statement.executeUpdate(String.format("INSERT INTO %s (UUID,name) VALUES ('%s','%s');", table, uuid, user.getName()));

					for (final StatsStorage.StatisticType stat : StatsStorage.StatisticType.values()) {
						if (!stat.isPersistent()) continue;

						user.setStat(stat, 0);
					}
				}
			} catch (SQLException exception) {
				exception.printStackTrace();
			}
		});
	}

	@NotNull
	public MysqlDatabase getDatabase() {
		return database;
	}

	public String getTable() {
		return table;
	}
}