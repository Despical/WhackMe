package me.despical.whackme.leaderboard;

import me.despical.commons.configuration.ConfigUtils;
import me.despical.whackme.WhackMe;
import me.despical.whackme.api.statistics.StatisticType;
import me.despical.whackme.user.data.UserDatabase;
import me.despical.whackme.user.data.MySQLManager;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Despical
 * <p>
 * Created at 8.11.2024
 */
public class LeaderboardManager {

    private final WhackMe plugin;
    private final Map<StatisticType, Leaderboard> leaderboards;

    public LeaderboardManager(WhackMe plugin) {
        this.plugin = plugin;
        this.leaderboards = new EnumMap<>(StatisticType.class);
        this.updateLeaderboards();
    }

    public Map.Entry<UUID, Integer> getEntry(StatisticType type, int placement) {
        return leaderboards.get(type).getEntry(placement);
    }

    public void updateLeaderboards() {
        for (StatisticType type : StatisticType.values()) {
            this.leaderboards.put(type, this.getLeaderboard(type));
        }

        this.leaderboards.values().forEach(Leaderboard::sort);
    }

    private Leaderboard getLeaderboard(StatisticType stat) {
        Leaderboard leaderboard = new Leaderboard();
        UserDatabase database = plugin.getUserManager().getUserDatabase();

        if (database instanceof MySQLManager) {
            MySQLManager mySQLManager = (MySQLManager) database;

            try (Connection connection = mySQLManager.getDatabase().getConnection()) {
                Statement statement = connection.createStatement();
                ResultSet set = statement.executeQuery(String.format("SELECT UUID, %s FROM %s ORDER BY %s", stat.getName(), mySQLManager.getTableName(), stat.getName()));

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

        for (String uuid : config.getKeys(false)) {
            leaderboard.addEntry(UUID.fromString(uuid), config.getInt(uuid + "." + stat.getName()));
        }

        return leaderboard;
    }
}
