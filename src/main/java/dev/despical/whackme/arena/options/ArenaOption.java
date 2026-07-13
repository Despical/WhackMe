package dev.despical.whackme.arena.options;

import lombok.Getter;

/**
 * @author Despical
 * <p>
 * Created at 12.12.2025
 */
@Getter
public abstract class ArenaOption<T> {

    private final String key;
    private final T defaultValue;
    private final Class<T> type;

    public ArenaOption(String key, T defaultValue, Class<T> type) {
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
        throw new UnsupportedOperationException("Parse method not implemented for option: " + key);
    }

    public boolean isPersistent() {
        return key != null && !key.isEmpty();
    }
}
