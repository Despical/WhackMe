package dev.despical.whackme.stats;

import lombok.Getter;

/**
 * @author Despical
 * <p>
 * Created at 8.11.2024
 */
@Getter
public abstract class StatisticType<T> {

    private final String key;
    private final T defaultValue;
    private final Class<T> type;

    public StatisticType(String key, T defaultValue, Class<T> type) {
        this.key = key;
        this.defaultValue = defaultValue;
        this.type = type;
    }

    public Object serialize(T value) {
        return value;
    }

    public T deserialize(Object value) {
        try {
            if (type.isInstance(value)) {
                return type.cast(value);
            }
            return parse(String.valueOf(value));
        } catch (Exception exception) {
            exception.printStackTrace();
            return defaultValue;
        }
    }

    protected T parse(String value) {
        throw new UnsupportedOperationException("Parse method not implemented for stat: " + key);
    }

    public boolean isPersistent() {
        return key != null && !key.isEmpty();
    }
}
