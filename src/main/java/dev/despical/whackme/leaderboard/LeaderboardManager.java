package dev.despical.whackme.leaderboard;

import dev.despical.whackme.WhackMe;
import dev.despical.whackme.stats.StatisticType;
import dev.despical.whackme.stats.Statistics;
import dev.despical.whackme.stats.offline.OfflineStats;
import dev.despical.whackme.user.User;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

/**
 * @author Despical
 * <p>
 * Created at 8.11.2024
 */
public class LeaderboardManager {

    private final WhackMe plugin;
    private final Map<String, Leaderboard<?>> leaderboards;

    public LeaderboardManager(WhackMe plugin) {
        this.plugin = plugin;
        this.leaderboards = new HashMap<>();
    }

    public void refreshAllLeaderboards() {
        refreshAllLeaderboards(Collections.emptyList());
    }

    public void refreshAllLeaderboards(Collection<User> activeUsers) {
        Map<UUID, OfflineStats> allPlayers = new HashMap<>();
        plugin.getDatabase().getAllPlayers().forEach(stats -> allPlayers.put(stats.getUuid(), stats));

        for (User user : activeUsers) {
            OfflineStats stats = allPlayers.computeIfAbsent(user.getUUID(), uuid -> new OfflineStats(uuid, user.getName()));
            for (StatisticType<?> type : Statistics.getPersistentStats()) {
                copyStatistic(user, stats, type);
            }
        }

        Set<OfflineStats> allPlayersCache = new HashSet<>(allPlayers.values());

        for (StatisticType<?> type : Statistics.getPersistentStats()) {
            if (type.getType() == Integer.class) {
                @SuppressWarnings("unchecked")
                StatisticType<Integer> intType = (StatisticType<Integer>) type;

                createLeaderboard(
                    intType.getKey(),
                    allPlayersCache,
                    stats -> stats.getStat(intType),
                    Comparator.<Integer>naturalOrder().reversed(),
                    0
                );
            }
        }
    }

    private <T> void copyStatistic(User user, OfflineStats stats, StatisticType<T> type) {
        stats.setStat(type, user.getStatistic(type));
    }

    private <T extends Comparable<T>> void createLeaderboard(String id, Set<OfflineStats> allPlayers, Function<OfflineStats, T> valueExtractor, Comparator<T> comparator, T fallbackValue) {
        List<LeaderboardEntry<T>> entries = allPlayers.stream()
            .map(stats -> new LeaderboardEntry<>(stats.getUuid(), stats.getName(), valueExtractor.apply(stats)))
            .filter(entry -> {
                if (entry.value() instanceof Number num) {
                    return num.doubleValue() > 0;
                }

                return true;
            })
            .sorted((e1, e2) -> comparator.compare(e1.value(), e2.value()))
            .limit(10)
            .toList();

        Leaderboard<T> leaderboard = new Leaderboard<>(id, entries, fallbackValue);
        leaderboards.put(id, leaderboard);
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public <T extends Comparable<T>> Leaderboard<T> getLeaderboard(String id) {
        return (Leaderboard<T>) leaderboards.get(id);
    }

    @Nullable
    public <T extends Comparable<T>> Leaderboard<T> getLeaderboard(StatisticType<T> type) {
        return getLeaderboard(type.getKey());
    }
}
