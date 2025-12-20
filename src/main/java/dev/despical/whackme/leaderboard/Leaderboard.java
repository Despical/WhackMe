package dev.despical.whackme.leaderboard;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Despical
 * <p>
 * Created at 8.11.2024
 */
public class Leaderboard {

    private final Map<UUID, Integer> entries;

    public Leaderboard() {
        this.entries = new LinkedHashMap<>();
    }

    public void addEntry(UUID uniqueId, int value) {
        entries.put(uniqueId, value);
    }

    public Map.Entry<UUID, Integer> getEntry(int placement) {
        return entries.entrySet()
            .stream()
            .skip(placement - 1)
            .findFirst()
            .orElse(null);
    }
}
