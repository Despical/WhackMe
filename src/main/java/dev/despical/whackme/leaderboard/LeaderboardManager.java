package dev.despical.whackme.leaderboard;

import dev.despical.commons.configuration.ConfigUtils;
import dev.despical.whackme.WhackMe;
import dev.despical.whackme.stat.Statistic;
import dev.despical.whackme.user.data.MySQLStatistics;
import dev.despical.whackme.user.data.UserDatabase;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Despical
 * <p>
 * Created at 8.11.2024
 */
public class LeaderboardManager {

    private final WhackMe plugin;
    private final Map<Statistic, Leaderboard> leaderboards;

    public LeaderboardManager(WhackMe plugin) {
        this.plugin = plugin;
        this.leaderboards = new HashMap<>();

        updateLeaderboards();
    }

    public Map.Entry<UUID, Integer> getEntry(Statistic type, int placement) {
        return leaderboards.get(type).getEntry(placement);
    }

    public void updateLeaderboards() {
        for (var type : Statistic.values()) {
            leaderboards.put(type, getLeaderboard(type));
        }
    }

    private Leaderboard getLeaderboard(Statistic stat) {
        Leaderboard leaderboard = new Leaderboard();
        UserDatabase database = plugin.getUserManager().getUserDatabase();

        if (database instanceof MySQLStatistics mySQLManager) {
            try (Connection connection = mySQLManager.getDatabase().getConnection();
                 Statement statement = connection.createStatement()
            ) {
                ResultSet set = statement.executeQuery(String.format("SELECT UUID, %s FROM %s ORDER BY %s DESC LIMIT 10", stat.getName(), mySQLManager.getTableName(), stat.getName()));

                while (set.next()) {
                    leaderboard.addEntry(UUID.fromString(set.getString("UUID")), set.getInt(stat.getName()));
                }

                return leaderboard;
            } catch (SQLException exception) {
                exception.printStackTrace();
                return null;
            }
        }

        FileConfiguration config = ConfigUtils.getConfig(plugin, "stats");
        List<String> stats = config.getKeys(false)
            .stream()
            .sorted(Comparator.comparingInt(uuid -> config.getInt(uuid + "." + stat.getName())).reversed())
            .limit(10)
            .collect(Collectors.toList());

        for (String uuid : stats) {
            leaderboard.addEntry(UUID.fromString(uuid), config.getInt(uuid + "." + stat.getName()));
        }

        return leaderboard;
    }
}
