package me.despical.whackme.user;

import me.despical.whackme.ConfigPreferences;
import me.despical.whackme.Main;
import me.despical.whackme.user.data.FileStats;
import me.despical.whackme.user.data.MysqlManager;
import me.despical.whackme.user.data.UserDatabase;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Despical
 * <p>
 * Created at 20.06.2022
 */
public class UserManager {

	private final Set<User> users;
	private final UserDatabase database;

	public UserManager(Main plugin) {
		this.users = new HashSet<>();
		this.database = plugin.getConfigPreferences().getOption(ConfigPreferences.Option.DATABASE_ENABLED) ? new MysqlManager() : new FileStats();

		plugin.getServer().getOnlinePlayers().forEach(this::loadStatistics);
	}

	public User getUser(Player player) {
		final var uuid = player.getUniqueId();

		for (final var user : users) {
			if (user.getUniqueId().equals(uuid)) {
				return user;
			}
		}

		final var user = new User(uuid);
		users.add(user);

		database.loadStatistics(user);
		return user;
	}

	public void loadStatistics(Player player) {
		database.loadStatistics(getUser(player));
	}

	public void removeUser(Player player) {
		users.remove(getUser(player));
	}

	public UserDatabase getDatabase() {
		return database;
	}
}