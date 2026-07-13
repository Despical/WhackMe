package dev.despical.whackme.leaderboard;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author Despical
 * <p>
 * Created at 08.11.2024
 */
public record Leaderboard<T extends Comparable<T>>(String id, List<LeaderboardEntry<T>> sortedEntries, T fallbackValue) {

    @NotNull
    public LeaderboardEntry<T> getEntryAtPosition(int pos) {
        pos -= 1;

        if (pos < 0 || pos >= sortedEntries.size()) {
            return LeaderboardEntry.empty(fallbackValue);
        }

        return sortedEntries.get(pos);
    }
}
