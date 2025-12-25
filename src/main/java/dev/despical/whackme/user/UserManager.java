package dev.despical.whackme.user;

import dev.despical.whackme.ConfigPreferences;
import dev.despical.whackme.WhackMe;
import dev.despical.whackme.user.data.UserDatabase;
import dev.despical.whackme.user.data.FileStatistics;
import dev.despical.whackme.user.data.MySQLStatistics;
import org.bukkit.Bukkit;
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
    private final UserDatabase userDatabase;

    public UserManager(WhackMe plugin) {
        this.users = new HashMap<>();
        this.userDatabase = plugin.getOption(ConfigPreferences.Option.DATABASE_ENABLED) ? new MySQLStatistics() : new FileStatistics();

        Bukkit.getOnlinePlayers().forEach(this::addUser);
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

        return addUser(player);
    }

    public Set<User> getUsers() {
        return new HashSet<>(users.values());
    }

    @NotNull
    public UserDatabase getUserDatabase() {
        return userDatabase;
    }
}
