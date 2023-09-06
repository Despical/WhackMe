package me.despical.whackme.user;

import me.despical.whackme.ConfigPreferences;
import me.despical.whackme.WhackMe;
import me.despical.whackme.user.data.FileStatistics;
import me.despical.whackme.user.data.IUserDatabase;
import me.despical.whackme.user.data.MysqlManager;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Despical
 * <p>
 * Created at 20.06.2022
 */
public class UserManager {

	@NotNull
	private final Set<User> users;

	@NotNull
	private final IUserDatabase userDatabase;

	public UserManager(WhackMe plugin) {
		this.users = new HashSet<>();
		this.userDatabase = plugin.getOption(ConfigPreferences.Option.DATABASE_ENABLED) ? new MysqlManager(plugin) : new FileStatistics(plugin);

		plugin.getServer().getOnlinePlayers().stream().map(this::getUser).forEach(this::loadStatistics);
	}

	@NotNull
	public User addUser(final Player player) {
		final var user = new User(player);

		this.users.add(user);
		return user;
	}

	public void removeUser(final Player player) {
		this.users.remove(this.getUser(player));
	}

	@NotNull
	public User getUser(final Player player) {
		final var uuid = player.getUniqueId();

		for (final var user : this.users) {
			if (uuid.equals(user.getUniqueId())) {
				return user;
			}
		}

        return this.addUser(player);
	}

	@NotNull
	public IUserDatabase getUserDatabase() {
		return this.userDatabase;
	}

	public void loadStatistics(final User user) {
		this.userDatabase.loadStatistics(user);
	}
}