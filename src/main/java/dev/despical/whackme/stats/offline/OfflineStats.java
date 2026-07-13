package dev.despical.whackme.stats.offline;

import dev.despical.whackme.stats.StatisticType;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Despical
 * <p>
 * Created at 27.01.2026
 */
public class OfflineStats {

    @Getter
    private final UUID uuid;

    @Getter
    private final String name;

    private final Map<StatisticType<?>, Object> stats;

    public OfflineStats(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
        this.stats = new HashMap<>();
    }

    public <T> void setStat(StatisticType<T> stat, T value) {
        stats.put(stat, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T getStat(StatisticType<T> type) {
        return (T) stats.computeIfAbsent(type, stat -> {
            if (stat.getDefaultValue() instanceof Map) {
                return new HashMap<>();
            }

            return stat.getDefaultValue();
        });
    }
}
