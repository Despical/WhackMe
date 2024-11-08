package me.despical.whackme.user;

import me.despical.whackme.ConfigPreferences;
import me.despical.whackme.WhackMe;
import me.despical.whackme.user.data.AbstractDatabase;
import me.despical.whackme.user.data.FileStatistics;
import me.despical.whackme.user.data.MySQLManager;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * @author Despical
 * <p>
 * Created at 20.06.2022
 */
public class UserManager {

	private final Map<UUID, User> users;
	private final AbstractDatabase userDatabase;

	public UserManager(WhackMe plugin) {
		this.users = new HashMap<>();
		this.userDatabase = plugin.getOption(ConfigPreferences.Option.DATABASE_ENABLED) ? new MySQLManager() : new FileStatistics();

		plugin.getServer().getOnlinePlayers().forEach(this::addUser);
	}

	@NotNull
	public User addUser(Player player) {
		User user = new User(player);
		users.put(player.getUniqueId(), user);

		userDatabase.loadStatistics(user);
		return user;
	}

	public void removeUser(Player player) {
		users.remove(player.getUniqueId());
	}

	@NotNull
	public User getUser(Player player) {
		User user = users.get(player.getUniqueId());

		if (user != null) {
			return user;
		}

		return this.addUser(player);
	}

	public Set<User> getUsers() {
		return new HashSet<>(users.values());
	}

	@NotNull
	public AbstractDatabase getUserDatabase() {
		return this.userDatabase;
	}
}