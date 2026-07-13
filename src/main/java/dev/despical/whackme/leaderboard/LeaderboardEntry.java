package dev.despical.whackme.leaderboard;

import java.util.UUID;

/**
 * @author Despical
 * <p>
 * Created at 01.06.2026
 */
public record LeaderboardEntry<T extends Comparable<T>>(UUID uuid, String name, T value) {

    public static <T extends Comparable<T>> LeaderboardEntry<T> empty(T fallbackValue) {
        return new LeaderboardEntry<>(UUID.randomUUID(), "No Player", fallbackValue);
    }
}
