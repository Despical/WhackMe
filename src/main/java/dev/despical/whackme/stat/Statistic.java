package dev.despical.whackme.stat;

/**
 * @author Despical
 * <p>
 * Created at 25.12.2025
 */
public enum Statistic implements StatisticType {

    TOURS_PLAYED("toursplayed"),
    RECORD_SCORE("recordscore"),
    PLUS_BLOCKS("whackedpluspointblocks"),
    MINUS_BLOCKS("whackedminuspointblocks"),
    LONGEST_STREAK("longeststreak");

    private final String name;
    private final int defaultValue;

    Statistic(String name) {
        this(name, 0);
    }

    Statistic(String name, int defaultValue) {
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

    @Override
    public boolean isPersistent() {
        return true;
    }
}
