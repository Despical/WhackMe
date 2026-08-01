package dev.despical.whackme.arena;

import dev.despical.commons.configuration.ConfigUtils;
import dev.despical.whackme.WhackMe;
import dev.despical.whackme.arena.options.ArenaKeys;
import dev.despical.whackme.arena.options.ArenaOption;
import lombok.RequiredArgsConstructor;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

/**
 * @author Despical
 * <p>
 * Created at 2.06.2026
 */
@RequiredArgsConstructor
public class ArenaDataSaver {

    private final WhackMe plugin;

    public void saveAllArenas() {
        ArenaRegistry arenaRegistry = plugin.getArenaRegistry();
        FileConfiguration config = arenaRegistry.getConfig();

        for (Arena arena : arenaRegistry.getArenas()) {
            saveArenaData(arena, config);
        }

        ConfigUtils.saveConfig(plugin, config, "arenas");
    }

    private void saveArenaData(Arena arena, FileConfiguration config) {
        String rootPath = arena.getId() + ".";

        for (ArenaOption<?> option : ArenaKeys.getPersistentKeys()) {
            saveSingleOption(arena, config, rootPath, option);
        }

        List<String> signLocations = plugin.getSignManager().getSerializedLocations(arena);
        config.set(rootPath + "signs", signLocations);
    }

    private <T> void saveSingleOption(Arena arena, FileConfiguration config, String rootPath, ArenaOption<T> option) {
        T value = arena.getOption(option);
        Object serializedValue = option.serialize(value);

        config.set(rootPath + option.getKey(), serializedValue);
    }
}
