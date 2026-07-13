package dev.despical.whackme.option;

import dev.despical.whackme.WhackMe;

/**
 * @author Despical
 * <p>
 * Created at 01.06.2026
 */
public interface ConfigOption<T> {

    String getPath();

    Class<T> getType();

    T getDefaultValue();

    default T value() {
        return WhackMe.getInstance().getOptions().get(this);
    }
}
