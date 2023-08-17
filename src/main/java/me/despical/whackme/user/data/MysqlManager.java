package me.despical.whackme.user.data;

import me.despical.commons.configuration.ConfigUtils;
import me.despical.commons.database.MysqlDatabase;
import me.despical.whackme.WhackMe;
import me.despical.whackme.api.StatsStorage;
import me.despical.whackme.user.User;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

/**
 * @author Despical
 * <p>
 * Created at 20.06.2022
 */
public non-sealed class MysqlManager extends IUserDatabase {

	private final String table;

	private MysqlDatabase database;

	public MysqlManager(WhackMe plugin) {
		super(plugin);
		this.table = ConfigUtils.getConfig(plugin, "mysql").getString("table", "wm_stats");

		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
			this.database = plugin.getMysqlDatabase();

			try (final var connection = database.getConnection()) {
				final var statement = connection.createStatement();

				statement.executeUpdate("""
						CREATE TABLE IF NOT EXISTS `%s` (
						  `UUID` char(36) NOT NULL PRIMARY KEY,
						  `name` varchar(32) NOT NULL,
						  `recordscore` int(11) NOT NULL DEFAULT '0',
						  `toursplayed` int(11) NOT NULL DEFAULT '0'
						);""".formatted(table));
			} catch (SQLException exception) {
				exception.fillInStackTrace();

				plugin.getLogger().severe("Couldn't create statistics table on MySQL database!");
			}
		});
	}

	@Override
	public void saveStatistic(@NotNull User user, StatsStorage.StatisticType statisticType) {
		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> database.executeUpdate("UPDATE %s SET %s=%d WHERE UUID='%s';".formatted(table, statisticType.getName(), user.getStat(statisticType), user.getUniqueId().toString())));
	}

	@Override
	public void saveStatistics(@NotNull User user) {
		final var builder = new StringBuilder(" SET ");

		for (final var stat : StatsStorage.StatisticType.values()) {
			if (!stat.isPersistent()) continue;

			final var name = stat.getName();
			final var value = user.getStat(stat);

			if (builder.toString().equalsIgnoreCase(" SET ")) {
				builder.append(name).append("=").append(value);
			}

			builder.append(", ").append(name).append("=").append(value);
		}

		final var update = builder.toString();

		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> database.executeUpdate("UPDATE %s%s WHERE UUID='%s';".formatted(table, update, user.getUniqueId().toString())));
	}

	@Override
	public void loadStatistics(@NotNull User user) {
		final var uuid = user.getUniqueId().toString();

		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
			try (final var connection = database.getConnection()) {
				final var statement = connection.createStatement();
				final var result = statement.executeQuery("SELECT * from %s WHERE UUID='%s';".formatted(table, uuid));

				if (result.next()) {
					for (final var stat : StatsStorage.StatisticType.values()) {
						if (!stat.isPersistent()) continue;

						user.setStat(stat, result.getInt(stat.getName()));
					}
				} else {
					statement.executeUpdate("INSERT INTO %s (UUID,name) VALUES ('%s','%s');".formatted(table, uuid, user.getName()));

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

	@NotNull
	public MysqlDatabase getDatabase() {
		return database;
	}

	public String getTable() {
		return table;
	}
}