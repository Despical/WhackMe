package dev.despical.whackme.api.statistics;

import dev.despical.whackme.user.User;
import lombok.Getter;

import java.util.stream.Stream;

/**
 * @author Despical
 * <p>
 * Created at 8.11.2024
 */
@Getter
public enum StatisticType {

    TOURS_PLAYED("toursplayed"),
    RECORD_SCORE("recordscore"),
    PLUS_BLOCKS("whackedpluspointblocks"),
    MINUS_BLOCKS("whackedminuspointblocks"),
    LONGEST_STREAK("longeststreak"),
    LOCAL_STREAK("local_streak", false),
    LOCAL_LONGEST_STREAK("local_longest_streak", false),
    LOCAL_SCORE("local_score", false);

    public static final StatisticType[] PERSISTENT_STATS = Stream.of(values()).filter(StatisticType::isPersistent).toArray(StatisticType[]::new);

    final String name;
    final boolean persistent;

    StatisticType(String name) {
        this(name, true);
    }

    StatisticType(String name, boolean persistent) {
        this.name = name;
        this.persistent = persistent;
    }

    public static StatisticType match(String name) {
        return Stream.of(values()).filter(statisticType -> statisticType.name.equals(name)).findFirst().orElse(null);
    }

    public String from(User user) {
        return Integer.toString(user.getStat(this));
    }
}
