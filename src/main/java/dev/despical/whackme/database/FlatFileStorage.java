package dev.despical.whackme.database;

import dev.despical.commons.configuration.ConfigUtils;
import dev.despical.whackme.stats.StatisticType;
import dev.despical.whackme.stats.Statistics;
import dev.despical.whackme.stats.offline.OfflineStats;
import dev.despical.whackme.user.User;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Despical
 * <p>
 * Created at 6.12.2025
 */
public final class FlatFileStorage extends Database {

    private final FileConfiguration config = ConfigUtils.getConfig(plugin, "data/stats");

    @Override
    public void loadData(User user) {
        String path = user.getUUID() + ".";

        for (StatisticType<?> type : Statistics.getPersistentStats()) {
            loadSingleStat(user, path, type);
        }
    }

    private <T> void loadSingleStat(User user, String path, StatisticType<T> type) {
        String fullPath = path + "stats." + type.getKey();

        if (config.contains(fullPath)) {
            Object rawValue = config.get(fullPath);
            T value = type.deserialize(rawValue);

            user.loadStatistic(type, value);
            return;
        }

        user.loadStatistic(type, type.getDefaultValue());
    }

    @Override
    public void saveData(User user) {
        String path = user.getUUID() + ".";
        config.set(path + "name", user.getName());

        for (StatisticType<?> type : Statistics.getPersistentStats()) {
            saveSingleStat(user, path, type);
        }
    }

    private <T> void saveSingleStat(User user, String path, StatisticType<T> type) {
        T value = user.getStatistic(type);
        Object serializedValue = type.serialize(value);

        config.set(path + "stats." + type.getKey(), serializedValue);
    }

    @Override
    @Nullable
    public OfflineStats loadOfflineData(OfflinePlayer player) {
        String path = player.getUniqueId() + ".";
        if (!config.contains(path + "name")) return null;

        String name = config.getString(path + "name");
        OfflineStats offlineStats = new OfflineStats(player.getUniqueId(), name);

        for (StatisticType<?> type : Statistics.getPersistentStats()) {
            loadSingleOfflineStat(offlineStats, path, type);
        }

        return offlineStats;
    }

    private <T> void loadSingleOfflineStat(OfflineStats offlineStats, String path, StatisticType<T> type) {
        String fullPath = path + "stats." + type.getKey();

        if (config.contains(fullPath)) {
            Object rawValue = config.get(fullPath);
            T value = type.deserialize(rawValue);

            offlineStats.setStat(type, value);
            return;
        }

        offlineStats.setStat(type, type.getDefaultValue());
    }

    @Override
    public Set<OfflineStats> getAllPlayers() {
        Set<OfflineStats> offlineStats = new HashSet<>();

        for (String uuid : config.getKeys(false)) {
            String name = config.getString(uuid + ".name");
            if (name == null) continue;

            OfflinePlayer player = Bukkit.getOfflinePlayerIfCached(name);
            if (player == null) continue;

            OfflineStats stats = loadOfflineData(player);
            if (stats != null) {
                offlineStats.add(stats);
            }
        }

        return offlineStats;
    }

    @Override
    public void saveAllData() {
        plugin.getUserManager().getUsers().forEach(this::saveData);
    }

    @Override
    public void shutdown() {
        for (User user : plugin.getUserManager().getUsers()) {
            saveData(user);
        }

        ConfigUtils.saveConfig(plugin, config, "data/stats");
    }
}
