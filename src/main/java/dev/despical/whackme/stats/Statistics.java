package dev.despical.whackme.stats;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Despical
 * <p>
 * Created at 25.12.2025
 */
public final class Statistics {

    private static final Gson GSON = new Gson();

    public static final StatisticType<Integer> GAMES_PLAYED = createIntStat("games_played");

    public static final StatisticType<Integer> PERFECT_RUNS = createIntStat("perfect_runs");
    public static final StatisticType<Integer> RECORD_SCORE = createIntStat("record_score");

    @SuppressWarnings("unchecked")
    public static final StatisticType<Map<String, Integer>> ARENA_BEST_SCORES = new StatisticType<>(
        "arena_best_scores",
        new HashMap<>(),
        (Class<Map<String, Integer>>) (Class<?>) Map.class
    ) {

        @Override
        public Object serialize(Map<String, Integer> value) {
            return GSON.toJson(value);
        }

        @Override
        protected Map<String, Integer> parse(String value) {
            Map<String, Integer> scores = GSON.fromJson(value, new TypeToken<Map<String, Integer>>() {}.getType());
            return scores != null ? scores : new HashMap<>();
        }
    };

    public static final StatisticType<Integer> LONGEST_HIT_STREAK = createIntStat("longest_hit_streak");

    public static final StatisticType<Integer> PLUS_BLOCKS = createIntStat("whacked_plus_blocks");
    public static final StatisticType<Integer> MINUS_BLOCKS = createIntStat("whacked_minus_blocks");

    public static final StatisticType<Integer> LOCAL_HIT_STREAK = createTempIntStat("local_hit_streak");
    public static final StatisticType<Integer> LOCAL_SCORE = createTempIntStat("local_score");
    public static final StatisticType<Integer> LOCAL_LONGEST_HIT_STREAK = createTempIntStat("local_longest_hit_streak");
    public static final StatisticType<Integer> LOCAL_CORRECT_BLOCKS = createTempIntStat("local_correct_blocks");
    public static final StatisticType<Integer> LOCAL_WRONG_BLOCKS = createTempIntStat("local_wrong_blocks");

    private static StatisticType<Integer> createIntStat(String key) {
        return new StatisticType<>(key, 0, Integer.class) {

            @Override
            protected Integer parse(String value) {
                try {
                    return Integer.parseInt(value);
                } catch (NumberFormatException ignored) {
                    return 0;
                }
            }
        };
    }

    private static StatisticType<Integer> createTempIntStat(String key) {
        return new StatisticType<>(null, 0, Integer.class) {

            @Override
            public String getKey() {
                return key;
            }

            @Override
            public boolean isPersistent() {
                return false;
            }
        };
    }

    public static List<StatisticType<?>> getAllStats() {
        return List.of(GAMES_PLAYED, PERFECT_RUNS, RECORD_SCORE, ARENA_BEST_SCORES, PLUS_BLOCKS, MINUS_BLOCKS, LONGEST_HIT_STREAK,
            LOCAL_HIT_STREAK, LOCAL_SCORE, LOCAL_LONGEST_HIT_STREAK, LOCAL_CORRECT_BLOCKS, LOCAL_WRONG_BLOCKS);
    }

    public static List<StatisticType<?>> getPersistentStats() {
        return getAllStats().stream().filter(StatisticType::isPersistent).toList();
    }

    public static List<StatisticType<?>> getTemporaryStats() {
        return getAllStats().stream().filter(stat -> !stat.isPersistent()).toList();
    }
}
