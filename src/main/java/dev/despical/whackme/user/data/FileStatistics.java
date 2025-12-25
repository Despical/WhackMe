package dev.despical.whackme.user.data;

import dev.despical.commons.configuration.ConfigUtils;
import dev.despical.whackme.stat.Statistic;
import dev.despical.whackme.user.User;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

/**
 * @author Despical
 * <p>
 * Created at 20.06.2022
 */
public class FileStatistics extends UserDatabase {

    private final FileConfiguration config;

    public FileStatistics() {
        this.config = ConfigUtils.getConfig(plugin, "stats");
    }

    @Override
    public void saveStatistic(@NotNull User user, Statistic stat) {
        config.set(user.getUniqueId().toString() + "." + stat.getName(), user.getStat(stat));

        ConfigUtils.saveConfig(plugin, config, "stats");
    }

    @Override
    public void saveStatistics(@NotNull User user) {
        String uuid = user.getUniqueId().toString();

        for (Statistic stat : Statistic.values()) {
            config.set(uuid + "." + stat.getName(), user.getStat(stat));
        }

        ConfigUtils.saveConfig(plugin, config, "stats");
    }

    @Override
    public void saveAllStatistics() {
        for (User user : plugin.getUserManager().getUsers()) {
            String uuid = user.getUniqueId().toString();

            for (Statistic stat : Statistic.values()) {
                config.set(uuid + "." + stat.getName(), user.getStat(stat));
            }
        }

        ConfigUtils.saveConfig(plugin, config, "stats");
    }

    @Override
    public void loadStatistics(@NotNull User user) {
        String uuid = user.getUniqueId().toString();

        for (Statistic stat : Statistic.values()) {
            user.setStat(stat, config.getInt(uuid + "." + stat.getName()));
        }
    }

    @Override
    public void shutdown() {
        this.saveAllStatistics();
    }
}
