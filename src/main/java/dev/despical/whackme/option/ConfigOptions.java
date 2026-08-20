package dev.despical.whackme.option;

import dev.despical.whackme.WhackMe;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * @author Despical
 * <p>
 * Created at 01.06.2026
 */
public class ConfigOptions {

    private final WhackMe plugin;
    private final Map<ConfigOption<?>, Object> options;

    public ConfigOptions(WhackMe plugin) {
        this.plugin = plugin;
        this.options = new HashMap<>();
        this.loadOptions();
    }

    public <T> T get(ConfigOption<T> option) {
        return option.getType().cast(options.computeIfAbsent(option, _ -> option.getDefaultValue()));
    }

    public boolean isEnabled(BooleanOption option) {
        return this.<Boolean>get(option);
    }

    public void reloadOptions() {
        plugin.reloadConfig();

        loadOptions();
    }

    private void loadOptions() {
        FileConfiguration config = plugin.getConfig();

        Stream.of(BooleanOption.values(), IntOption.values())
            .flatMap(Arrays::stream)
            .forEach(option -> options.put(option, config.get(option.getPath(), option.getDefaultValue())));
    }
}
