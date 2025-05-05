package me.despical.whackme.user.data;

import me.despical.commons.configuration.ConfigUtils;
import me.despical.commons.database.MySQLDatabase;
import me.despical.whackme.api.statistics.StatisticType;
import me.despical.whackme.user.User;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;

/**
 * @author Despical
 * <p>
 * Created at 20.06.2022
 */
public class MySQLStatistics extends UserDatabase {

    private final String tableName;
    private final MySQLDatabase database;
    private final ExecutorService executor;

    public MySQLStatistics() {
        FileConfiguration config = ConfigUtils.getConfig(plugin, "mysql");

        this.tableName = config.getString("table", "wm_stats");
        this.database = new MySQLDatabase(plugin, config);
        this.executor = Executors.newSingleThreadExecutor();
        
        executor.submit(() -> {
            try (Connection connection = database.getConnection();
                Statement statement = connection.createStatement()            ) {
                statement.executeUpdate(String.format(
                    "CREATE TABLE IF NOT EXISTS `%s` (\n" +
                        "`UUID` CHAR(36) PRIMARY KEY,\n" +
                        "`name` VARCHAR(32) NOT NULL,\n" +
                        "`recordscore` INT NOT NULL DEFAULT 0,\n" +
                        "`toursplayed` INT NOT NULL DEFAULT 0,\n" +
                        "`whackedpluspointblocks` INT NOT NULL DEFAULT 0,\n" +
                        "`whackedminuspointblocks` INT NOT NULL DEFAULT 0,\n" +
                        "`longeststreak` INT NOT NULL DEFAULT 0);",
                    tableName));
            } catch (SQLException exception) {
                plugin.getLogger().log(Level.SEVERE, "Could not create the statistics table!", exception);
            }
        });
    }

    @Override
    public void saveStatistic(@NotNull User user, StatisticType statisticType) {
        executor.submit(() -> database.executeUpdate(String.format("UPDATE `%s` SET %s=%d WHERE `UUID` = '%s';", tableName, statisticType.getName(), user.getStat(statisticType), user.getUniqueId().toString())));
    }

    @Override
    public void saveStatistics(@NotNull User user) {
        executor.submit(() -> {
            String update = getUpdateStatement(user);

            database.executeUpdate(String.format("UPDATE %s%s WHERE `UUID` = '%s';", tableName, update, user.getUniqueId().toString()));
        });
    }

    @Override
    public void saveAllStatistics() {
        for (User user : plugin.getUserManager().getUsers()) {
            String update = getUpdateStatement(user);

            database.executeUpdate(String.format("UPDATE %s%s WHERE `UUID` = '%s';", tableName, update, user.getUniqueId().toString()));
        }
    }

    @Override
    public void loadStatistics(@NotNull User user) {
        executor.submit(() -> {
            try (Connection connection = database.getConnection();
                 Statement statement = connection.createStatement()
            ) {
                String uuid = user.getUniqueId().toString();
                ResultSet result = statement.executeQuery(String.format("SELECT * FROM `%s` WHERE `UUID` = '%s';", tableName, uuid));

                if (result.next()) {
                    for (StatisticType stat : StatisticType.PERSISTENT_STATS) {
                        user.setStat(stat, result.getInt(stat.getName()));
                    }

                    return;
                }

                statement.executeUpdate(String.format("INSERT INTO `%s` (`UUID`, `name`) VALUES ('%s', '%s');", tableName, uuid, user.getName()));
            } catch (SQLException exception) {
                exception.printStackTrace();
            }
        });
    }

    @Override
    public void shutdown() {
        saveAllStatistics();

        executor.shutdown();
        database.shutdownConnPool();
    }

    @NotNull
    public MySQLDatabase getDatabase() {
        return database;
    }

    @NotNull
    public String getTableName() {
        return tableName;
    }

    private String getUpdateStatement(User user) {
        StringBuilder builder = new StringBuilder(" SET ");

        for (StatisticType stat : StatisticType.PERSISTENT_STATS) {
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
