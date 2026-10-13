package dev.despical.whackme.leaderboard;

import dev.despical.whackme.WhackMe;
import dev.despical.whackme.arena.Arena;
import dev.despical.whackme.option.IntOption;
import dev.despical.whackme.stats.StatisticType;
import dev.despical.whackme.stats.Statistics;
import dev.despical.whackme.stats.offline.OfflineStats;
import dev.despical.whackme.user.User;
import org.bukkit.Bukkit;
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
    private final Map<UUID, OfflineStats> players = new HashMap<>();
    private final Set<UUID> updatedPlayers = new HashSet<>();
    private final Map<String, LeaderboardIndex> indexes = new HashMap<>();
    private volatile Map<String, Leaderboard<?>> leaderboards = Map.of();

    public LeaderboardManager(WhackMe plugin) {
        this.plugin = plugin;
    }

    public void refreshAllLeaderboards() {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            Set<OfflineStats> storedPlayers = plugin.getDatabase().getAllPlayers();
            plugin.getServer().getScheduler().runTask(plugin, () -> rebuildLeaderboards(storedPlayers));
        });
    }

    public void refreshAllLeaderboards(Collection<User> activeUsers) {
        List<User> users = List.copyOf(activeUsers);
        runOnMainThread(() -> {
            users.forEach(this::updateLeaderboards);
            refreshAllLeaderboards();
        });
    }

    public void updateLeaderboards(User user) {
        runOnMainThread(() -> {
            OfflineStats stats = snapshot(user);
            players.put(stats.getUuid(), stats);
            updatedPlayers.add(stats.getUuid());

            updateIndexes(stats);
            publishLeaderboards();
        });
    }

    private OfflineStats snapshot(User user) {
        OfflineStats stats = new OfflineStats(user.getUUID(), user.getName());
        for (StatisticType<?> type : Statistics.getPersistentStats()) {
            copyStatistic(user, stats, type);
        }
        stats.setStat(Statistics.ARENA_BEST_SCORES, Map.copyOf(user.getStatistic(Statistics.ARENA_BEST_SCORES)));
        return stats;
    }

    private <T> void copyStatistic(User user, OfflineStats stats, StatisticType<T> type) {
        stats.setStat(type, user.getStatistic(type));
    }

    private void rebuildLeaderboards(Set<OfflineStats> storedPlayers) {
        Map<UUID, OfflineStats> refreshedPlayers = new HashMap<>();
        storedPlayers.forEach(stats -> refreshedPlayers.put(stats.getUuid(), stats));

        plugin.getUserManager().getUsers().stream()
            .filter(user -> updatedPlayers.contains(user.getUUID()))
            .forEach(user -> players.put(user.getUUID(), snapshot(user)));

        // Local results stay authoritative while asynchronous database saves are in flight.
        updatedPlayers.forEach(uuid -> refreshedPlayers.put(uuid, players.get(uuid)));
        players.clear();
        players.putAll(refreshedPlayers);

        indexes.clear();
        updateIndexes(null);
        publishLeaderboards();
    }

    private void updateIndexes(OfflineStats updatedPlayer) {
        Set<String> currentIds = new HashSet<>();

        for (StatisticType<?> type : Statistics.getPersistentStats()) {
            if (type.getType() != Integer.class) {
                continue;
            }

            @SuppressWarnings("unchecked")
            StatisticType<Integer> intType = (StatisticType<Integer>) type;
            updateIndex(intType.getKey(), stats -> stats.getStat(intType), updatedPlayer);
            currentIds.add(intType.getKey());
        }

        for (Arena arena : plugin.getArenaRegistry().getArenas()) {
            String arenaId = arena.getId();
            String id = "arena_score_" + arenaId;
            updateIndex(id, stats -> stats.getStat(Statistics.ARENA_BEST_SCORES).getOrDefault(arenaId, 0), updatedPlayer);
            currentIds.add(id);
        }

        indexes.keySet().retainAll(currentIds);
    }

    private void updateIndex(String id, Function<OfflineStats, Integer> valueExtractor, OfflineStats updatedPlayer) {
        LeaderboardIndex index = indexes.computeIfAbsent(id, key -> {
            LeaderboardIndex created = new LeaderboardIndex(key);
            players.values().forEach(stats -> created.update(entry(stats, valueExtractor)));
            return created;
        });

        if (updatedPlayer != null) {
            index.update(entry(updatedPlayer, valueExtractor));
        }
    }

    private LeaderboardEntry<Integer> entry(OfflineStats stats, Function<OfflineStats, Integer> valueExtractor) {
        return new LeaderboardEntry<>(stats.getUuid(), stats.getName(), valueExtractor.apply(stats));
    }

    private void publishLeaderboards() {
        Map<String, Leaderboard<?>> snapshot = new HashMap<>();
        int size = IntOption.LEADERBOARD_SIZE.value();
        indexes.forEach((id, index) -> snapshot.put(id, index.snapshot(size)));
        leaderboards = Map.copyOf(snapshot);
    }

    private void runOnMainThread(Runnable task) {
        if (Bukkit.isPrimaryThread()) {
            task.run();
        } else {
            plugin.getServer().getScheduler().runTask(plugin, task);
        }
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
