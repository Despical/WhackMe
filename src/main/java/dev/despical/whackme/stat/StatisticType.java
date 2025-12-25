package dev.despical.whackme.stat;

/**
 * @author Despical
 * <p>
 * Created at 8.11.2024
 */
public sealed interface StatisticType permits Statistic, LocalStatistic {

    String getName();

    int getDefaultValue();

    default boolean isPersistent() {
        return false;
    }
}
