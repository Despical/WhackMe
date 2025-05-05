package me.despical.whackme.api.statistics;

import me.despical.commons.configuration.ConfigUtils;
import me.despical.commons.sorter.SortUtils;
import me.despical.whackme.WhackMe;
import me.despical.whackme.user.data.MySQLStatistics;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Despical
 * <p>
 * Created at 20.06.2022
 */
public class StatsStorage {

    private static final WhackMe plugin = WhackMe.getInstance();

    @NotNull
    public static Map<UUID, Integer> getStats(StatisticType stat) {
        if (plugin.getUserManager().getUserDatabase() instanceof MySQLStatistics) {
            MySQLStatistics mysqlManager = (MySQLStatistics) plugin.getUserManager().getUserDatabase();

            try (Connection connection = mysqlManager.getDatabase().getConnection();
                 Statement statement = connection.createStatement()
            ) {
                ResultSet set = statement.executeQuery(String.format("SELECT UUID, %s FROM %s ORDER BY %s DESC LIMIT 10", stat.getName(), mysqlManager.getTableName(), stat.getName()));
                Map<UUID, Integer> column = new HashMap<>();

                while (set.next()) {
                    column.put(UUID.fromString(set.getString("UUID")), set.getInt(stat.getName()));
                }

                return column;
            } catch (SQLException e) {
                plugin.getLogger().warning("SQLException occurred during getting statistics from database!");
                return new HashMap<>();
            }
        }

        FileConfiguration config = ConfigUtils.getConfig(plugin, "stats");
        Map<UUID, Integer> stats = new HashMap<>();

        for (String string : config.getKeys(false)) {
            stats.put(UUID.fromString(string), config.getInt(string + "." + stat.getName()));
        }

        return SortUtils.sortByValue(stats);
    }

    public static int getUserStats(Player player, StatisticType statisticType) {
        return plugin.getUserManager().getUser(player).getStat(statisticType);
    }
}
