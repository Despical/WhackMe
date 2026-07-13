package dev.despical.whackme.option;

/**
 * @author Despical
 * <p>
 * Created at 01.06.2026
 */
public enum IntOption implements ConfigOption<Integer> {

    GAMEPLAY_TIME("game-settings.gameplay-time", 30),
    GAME_COOLDOWN("game-settings.cooldown-seconds", 0),
    HIT_COOLDOWN_DELAY("game-settings.hit-cooldown-delay", 20),
    POINT_BLOCK_TICKS("point-blocks.ticks", 8),
    POINT_BLOCK_WAIT_MILLISECONDS("point-blocks.wait-milliseconds", 12);

    private final String path;
    private final int defaultValue;

    IntOption(String path, int defaultValue) {
        this.path = path;
        this.defaultValue = defaultValue;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public Class<Integer> getType() {
        return Integer.class;
    }

    @Override
    public Integer getDefaultValue() {
        return defaultValue;
    }
}
