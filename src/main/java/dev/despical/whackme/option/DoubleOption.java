package dev.despical.whackme.option;

/**
 * @author Despical
 * <p>
 * Created at 01.06.2026
 */
public enum DoubleOption implements ConfigOption<Double> {

    POINT_BLOCK_Y_MULTIPLIER("point-blocks.y-multiplier", 0.05),
    POINT_BLOCK_MAX_Y_MULTIPLIER("point-blocks.max-y-multiplier", 0.64);

    private final String path;
    private final double defaultValue;

    DoubleOption(String path, double defaultValue) {
        this.path = path;
        this.defaultValue = defaultValue;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public Class<Double> getType() {
        return Double.class;
    }

    @Override
    public Double getDefaultValue() {
        return defaultValue;
    }
}
