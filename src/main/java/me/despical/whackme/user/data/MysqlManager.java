package me.despical.whackme.user.data;

import me.despical.commons.database.MysqlDatabase;
import me.despical.whackme.api.StatsStorage;
import me.despical.whackme.user.User;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

/**
 * @author Despical
 * <p>
 * Created at 20.06.2022
 */
public class MysqlManager implements UserDatabase {

	private final MysqlDatabase database;

	public MysqlManager() {
		this.database = plugin.getMysqlDatabase();

		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
			try (final var connection = database.getConnection()) {
				final var statement = connection.createStatement();

				statement.executeUpdate("""
						CREATE TABLE IF NOT EXISTS `playerstats` (
						  `UUID` char(36) NOT NULL PRIMARY KEY,
						  `name` varchar(32) NOT NULL,
						  `recordscore` int(11) NOT NULL DEFAULT '0',
						  `toursplayed` int(11) NOT NULL DEFAULT '0'
						);""");
			} catch (SQLException exception) {
				exception.printStackTrace();

				plugin.getLogger().warning("Can not save contents to MySQL database!");
			}
		});
	}

	@Override
	public void saveStatistic(User user, StatsStorage.StatisticType stat) {
		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> database.executeUpdate("UPDATE playerstats SET %s=%d WHERE UUID='%s';".formatted(stat.getName(), user.getStat(stat), user.getUniqueId().toString())));
	}

	@Override
	public void saveAllStatistic(@NotNull User user) {
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
		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> database.executeUpdate("UPDATE playerstats%s WHERE UUID='%s';".formatted(update, user.getUniqueId().toString())));
	}

	@Override
	public void loadStatistics(@NotNull User user) {
		final String uuid = user.getUniqueId().toString(), name = user.getPlayer().getName();

		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
			try (final var connection = database.getConnection()) {
				final var statement = connection.createStatement();
				final var result = statement.executeQuery("SELECT * from playerstats WHERE UUID='%s';".formatted(uuid));

				if (result.next()) {
					for (final var stat : StatsStorage.StatisticType.values()) {
						if (!stat.isPersistent()) continue;

						user.setStat(stat, result.getInt(stat.getName()));
					}
				} else {
					statement.executeUpdate("INSERT INTO playerstats (UUID,name) VALUES ('%s','%s');".formatted(uuid, name));

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

	public MysqlDatabase getDatabase() {
		return database;
	}
}