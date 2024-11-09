package me.despical.whackme.user.data;

import me.despical.commons.configuration.ConfigUtils;
import me.despical.commons.database.MysqlDatabase;
import me.despical.whackme.api.statistics.StatisticType;
import me.despical.whackme.user.User;
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
public class MySQLManager extends AbstractDatabase {

    private final String tableName;
    private final MysqlDatabase database;

    public MySQLManager() {
        this.tableName = ConfigUtils.getConfig(plugin, "mysql").getString("table", "wm_stats");
        this.database = new MysqlDatabase(plugin, "mysql");

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection connection = database.getConnection()) {
                Statement statement = connection.createStatement();

                statement.executeUpdate(String.format(
                    "CREATE TABLE IF NOT EXISTS `%s` (\n" +
                        "`UUID` char(36) NOT NULL PRIMARY KEY,\n" +
                        "`name` varchar(32) NOT NULL,\n" +
                        "`recordscore` int(11) NOT NULL DEFAULT '0',\n" +
                        "`toursplayed` int(11) NOT NULL DEFAULT '0',\n" +
                        "`whackedpluspointblocks` int(11) NOT NULL DEFAULT '0',\n" +
                        "`whackedminuspointblocks` int(11) NOT NULL DEFAULT '0',\n" +
                        "`longeststreak` int(11) NOT NULL DEFAULT '0');",
                    tableName));
            } catch (SQLException exception) {
                exception.printStackTrace();
            }
        });
    }

    @Override
    public void saveStatistic(@NotNull User user, StatisticType statisticType) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> database.executeUpdate(String.format("UPDATE %s SET %s=%d WHERE UUID='%s';", tableName, statisticType.getName(), user.getStat(statisticType), user.getUniqueId().toString())));
    }

    @Override
    public void saveStatistics(@NotNull User user) {
        String update = this.getUpdateStatement(user);

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> database.executeUpdate(String.format("UPDATE %s%s WHERE UUID='%s';", tableName, update, user.getUniqueId().toString())));
    }

    @Override
    public void saveAllStatistics() {
        for (User user : plugin.getUserManager().getUsers()) {
            String update = this.getUpdateStatement(user);

            database.executeUpdate(String.format("UPDATE %s%s WHERE UUID='%s';", tableName, update, user.getUniqueId().toString()));
        }
    }


    @Override
    public void loadStatistics(@NotNull User user) {
        String uuid = user.getUniqueId().toString();

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection connection = database.getConnection()) {
                Statement statement = connection.createStatement();
                ResultSet result = statement.executeQuery(String.format("SELECT * from %s WHERE UUID='%s';", tableName, uuid));

                if (result.next()) {
                    for (StatisticType stat : StatisticType.values()) {
                        if (!stat.isPersistent()) continue;

                        user.setStat(stat, result.getInt(stat.getName()));
                    }
                } else {
                    statement.executeUpdate(String.format("INSERT INTO %s (UUID,name) VALUES ('%s','%s');", tableName, uuid, user.getName()));

                    for (StatisticType stat : StatisticType.values()) {
                        if (!stat.isPersistent()) continue;

                        user.setStat(stat, 0);
                    }
                }
            } catch (SQLException exception) {
                exception.printStackTrace();
            }
        });
    }

    @Override
    public void shutdown() {
        this.saveAllStatistics();
        this.database.shutdownConnPool();
    }

    @NotNull
    public MysqlDatabase getDatabase() {
        return database;
    }

    @NotNull
    public String getTableName() {
        return tableName;
    }

    private String getUpdateStatement(User user) {
        StringBuilder builder = new StringBuilder(" SET ");

        for (StatisticType stat : StatisticType.values()) {
            if (!stat.isPersistent()) continue;

            String name = stat.getName();
            int value = user.getStat(stat);

            if (builder.toString().equalsIgnoreCase(" SET ")) {
                builder.append(name).append("=").append(value);
            }

            builder.append(", ").append(name).append("=").append(value);
        }

        return builder.toString();
    }
}