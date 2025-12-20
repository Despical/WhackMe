package dev.despical.whackme;

import dev.despical.commons.serializer.InventorySerializer;
import dev.despical.commons.string.StringUtils;
import dev.despical.whackme.api.Reloadable;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

/**
 * @author Despical
 * <p>
 * Created at 20.06.2022
 */
public class ConfigPreferences implements Reloadable {

    private final WhackMe plugin;
    private final Map<Option, Boolean> options;

    private double pointBlockMultiplier;
    private long ticks;
    private boolean isAsync;

    public ConfigPreferences(WhackMe plugin) {
        this.plugin = plugin;
        this.options = new HashMap<>();
        this.loadOptions();
    }

    @Override
    public void reload() {
        WhackMe.getInstance().reloadConfig();

        this.loadOptions();
    }

    public double getPointBlockMultiplier() {
        return pointBlockMultiplier;
    }

    public long getTicks() {
        return ticks;
    }

    public boolean getOption(Option option) {
        return options.get(option);
    }

    public boolean isAsync() {
        return isAsync;
    }

    private void loadOptions() {
        this.options.clear();

        FileConfiguration config = plugin.getConfig();

        for (Option option : Option.values()) {
            options.put(option, config.getBoolean(option.path, option.def));
        }

        this.pointBlockMultiplier = Math.min(config.getDouble("Point-Blocks.Y-Multiplier"), .64);
        this.ticks = config.getLong("Point-Blocks.Ticks", 8);
        this.isAsync = config.getBoolean("Point-Blocks.Run-Async");
    }

    public enum Option {

        BLOCK_COMMANDS(false),
        BLOCK_LEAVE_COMMAND(false),
        BOSS_BAR_ENABLED,
        CHAT_FORMAT_ENABLED,
        CLEAR_EFFECTS,
        CLEAR_INVENTORY,
        DATABASE_ENABLED(false),
        INVENTORY_MANAGER_ENABLED((config) -> {
            List<String> list = config.getStringList("Inventory-Manager.Do-Not-Restore");
            list.forEach(InventorySerializer::addNonSerializableElements);

            return config.getBoolean("Inventory-Manager.Enabled");
        }),
        UPDATE_NOTIFIER_ENABLED;

        private final String path;
        private final boolean def;

        Option() {
            this(true);
        }

        Option(boolean def) {
            this.def = def;
            this.path = StringUtils.capitalize(name().replace('_', '-').toLowerCase(Locale.ENGLISH), '-', '.');
        }

        Option(Function<FileConfiguration, Boolean> supplier) {
            this.path = "";
            this.def = supplier.apply(WhackMe.getInstance().getConfig());
        }
    }
}
