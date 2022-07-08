package me.despical.whackme.user.data;

import me.despical.whackme.Main;
import me.despical.whackme.api.StatsStorage;
import me.despical.whackme.user.User;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * @author Despical
 * <p>
 * Created at 20.06.2022
 */
public interface UserDatabase {

	Main plugin = JavaPlugin.getPlugin(Main.class);

	void saveStatistic(User user, StatsStorage.StatisticType stat);

	void saveAllStatistic(User user);

	void loadStatistics(User user);
}