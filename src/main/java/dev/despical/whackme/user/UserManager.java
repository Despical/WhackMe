package dev.despical.whackme.user;

import dev.despical.whackme.WhackMe;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * @author Despical
 * <p>
 * Created at 20.06.2022
 */
public class UserManager {

    private final WhackMe plugin;
    private final Map<UUID, User> users;

    public UserManager(WhackMe plugin) {
        this.plugin = plugin;
        this.users = new HashMap<>();
        this.loadDataOfOnlinePlayers();
    }

    public User getUser(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        return player == null ? null : this.getUser(player);
    }

    public User getUser(Player player) {
        User user = users.get(player.getUniqueId());

        if (user != null) {
            return user;
        }

        return createNewUser(player);
    }

    public void removeUser(User user) {
        users.remove(user.getUUID());
    }

    public Set<User> getUsers() {
        return Set.copyOf(users.values());
    }

    public User createNewUser(Player player) {
        User user = new User(player);
        users.put(player.getUniqueId(), user);

        plugin.getDatabase().loadData(user);
        return user;
    }

    private void loadDataOfOnlinePlayers() {
        plugin.getServer().getOnlinePlayers().forEach(this::createNewUser);
    }
}
