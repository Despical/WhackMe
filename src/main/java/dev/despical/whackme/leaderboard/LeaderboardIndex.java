package dev.despical.whackme.leaderboard;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.UUID;

/**
 * Keeps all positive scores ordered so a player update does not sort the full leaderboard.
 *
 * @author Despical
 * <p>
 * Created at 13.10.2026
 */
final class LeaderboardIndex {

    private final String id;
    private final Map<UUID, LeaderboardEntry<Integer>> players = new HashMap<>();
    private final NavigableSet<LeaderboardEntry<Integer>> entries = new TreeSet<>(
        Comparator.comparing(LeaderboardEntry<Integer>::value).reversed()
            .thenComparing(LeaderboardEntry::uuid)
    );

    LeaderboardIndex(String id) {
        this.id = id;
    }

    void update(LeaderboardEntry<Integer> entry) {
        LeaderboardEntry<Integer> previous = players.remove(entry.uuid());
        if (previous != null) {
            entries.remove(previous);
        }

        if (entry.value() > 0) {
            players.put(entry.uuid(), entry);
            entries.add(entry);
        }
    }

    Leaderboard<Integer> snapshot(int size) {
        return new Leaderboard<>(id, entries.stream().limit(size).toList(), 0);
    }
}
