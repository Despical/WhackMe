package dev.despical.whackme.database;

import dev.despical.whackme.WhackMe;
import dev.despical.whackme.stats.offline.OfflineStats;
import dev.despical.whackme.user.User;
import org.bukkit.OfflinePlayer;

import java.util.Set;

/**
 * @author Despical
 * <p>
 * Created at 6.12.2025
 */
public sealed abstract class Database permits FlatFileStorage, MySQLStorage {

    protected static final WhackMe plugin = WhackMe.getInstance();

    public abstract void loadData(User user);

    public abstract OfflineStats loadOfflineData(OfflinePlayer player);

    public abstract Set<OfflineStats> getAllPlayers();

    public abstract void saveData(User user);

    public abstract void saveAllData();

    public abstract void shutdown();
}
