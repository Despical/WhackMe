package dev.despical.whackme.stat;

/**
 * @author Despical
 * <p>
 * Created at 25.12.2025
 */
public enum LocalStatistic implements StatisticType {

    STREAK("streak"),
    LONGEST_STREAK("longest_streak"),
    SCORE("score");

    private final String name;
    private final int defaultValue;

    LocalStatistic(String name) {
        this(name, 0);
    }

    LocalStatistic(String name, int defaultValue) {
        this.name = name;
        this.defaultValue = defaultValue;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getDefaultValue() {
        return defaultValue;
    }
}
