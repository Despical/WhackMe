package dev.despical.whackme.arena.options;

import dev.despical.whackme.WhackMe;
import lombok.Getter;

import java.util.function.Supplier;
import java.util.logging.Level;

/**
 * @author Despical
 * <p>
 * Created at 12.12.2025
 */
public abstract class ArenaOption<T> {

    private final @Getter String key;
    private final @Getter Class<T> type;
    private final T defaultValue;
    private final Supplier<? extends T> defaultSupplier;

    public ArenaOption(String key, Class<T> type, T defaultValue) {
        this.key = key;
        this.defaultValue = defaultValue;
        this.defaultSupplier = null;
        this.type = type;
    }

    public ArenaOption(String key, Class<T> type, Supplier<? extends T> defaultSupplier) {
        this.key = key;
        this.defaultValue = null;
        this.defaultSupplier = defaultSupplier;
        this.type = type;
    }

    public T getDefaultValue() {
        return defaultSupplier == null ? defaultValue : defaultSupplier.get();
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
            WhackMe.getInstance().getLogger().log(Level.WARNING, "Could not read stored value for option '" + key + "', using the default instead.", exception);
            return getDefaultValue();
        }
    }

    protected T parse(String value) {
        throw new UnsupportedOperationException("Parse method not implemented for option: " + key);
    }

    public boolean isPersistent() {
        return key != null && !key.isEmpty();
    }
}
