package dev.despical.whackme.stats.offline;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import dev.despical.whackme.WhackMe;
import org.bukkit.OfflinePlayer;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author Despical
 * <p>
 * Created at 27.01.2026
 */
public class StatsCacheManager {

    private final WhackMe plugin;
    private final Cache<UUID, OfflineStats> offlineCache;

    public StatsCacheManager(WhackMe plugin) {
        this.plugin = plugin;
        this.offlineCache = CacheBuilder.newBuilder()
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .maximumSize(1000)
            .build();
    }

    public OfflineStats getStats(OfflinePlayer player) {
        UUID uuid = player.getUniqueId();
        OfflineStats cached = offlineCache.getIfPresent(uuid);

        if (cached != null) {
            return cached;
        }

        OfflineStats loaded = plugin.getDatabase().loadOfflineData(player);
        if (loaded == null) {
            loaded = new OfflineStats(uuid, player.getName());
        }

        offlineCache.put(uuid, loaded);
        return loaded;
    }

    public void invalidate(UUID uuid) {
        offlineCache.invalidate(uuid);
    }
}
