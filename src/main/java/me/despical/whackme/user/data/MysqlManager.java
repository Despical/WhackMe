package me.despical.whackme.user.data;

import me.despical.commons.configuration.ConfigUtils;
import me.despical.commons.database.MysqlDatabase;
import me.despical.whackme.api.StatsStorage;
import me.despical.whackme.user.User;

import java.sql.SQLException;

/**
 * @author Despical
 * <p>
 * Created at 20.06.2022
 */
public class MysqlManager implements UserDatabase {

	private final String tableName;
	private final MysqlDatabase database;

	public MysqlManager() {
		this.tableName = ConfigUtils.getConfig(plugin, "mysql").getString("table", "playerstats");
		this.database = plugin.getMysqlDatabase();

		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
			try (final var connection = database.getConnection()) {
				final var statement = connection.createStatement();
				statement.executeUpdate("CREATE TABLE IF NOT EXISTS `" + tableName + "` (\n"
					+ "  `UUID` char(36) NOT NULL PRIMARY KEY,\n"
					+ "  `name` varchar(32) NOT NULL,\n"
					+ "  `recordscore` int(11) NOT NULL DEFAULT '0',\n"
					+ "  `toursplayed` int(11) NOT NULL DEFAULT '0'\n"
					+ ");");
			} catch (SQLException exception) {
				exception.printStackTrace();

				plugin.getLogger().warning("Can not save contents to MySQL database!");
			}
		});
	}

	@Override
	public void saveStatistic(User user, StatsStorage.StatisticType stat) {
		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
			final var query = "UPDATE " + tableName + " SET " + stat.getName() + "=" + user.getStat(stat)+ " WHERE UUID='" + user.getUniqueId().toString() + "';";

			database.executeUpdate(query);
		});
	}

	@Override
	public void saveAllStatistic(User user) {
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
		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> database.executeUpdate("UPDATE " + tableName + update + " WHERE UUID='" + user.getUniqueId().toString() + "';"));
	}

	@Override
	public void loadStatistics(User user) {
		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
			final String uuid = user.getUniqueId().toString(), name = user.getPlayer().getName();

			try (final var connection = database.getConnection()) {
				final var statement = connection.createStatement();
				final var resultSet = statement.executeQuery("SELECT * from " + tableName + " WHERE UUID='" + uuid + "';");

				if (resultSet.next()) {
					for (final var stat : StatsStorage.StatisticType.values()) {
						if (!stat.isPersistent()) continue;

						user.setStat(stat, resultSet.getInt(stat.getName()));
					}
				} else {
					statement.executeUpdate("INSERT INTO " + tableName + " (UUID,name) VALUES ('" + uuid + "','" + name + "');");

					for (final var stat : StatsStorage.StatisticType.values()) {
						if (!stat.isPersistent()) continue;

						user.setStat(stat, 0);
					}
				}
			} catch (SQLException exception) {
				exception.printStackTrace();
			}
		});
	}

	public String getTableName() {
		return tableName;
	}

	public MysqlDatabase getDatabase() {
		return database;
	}
}