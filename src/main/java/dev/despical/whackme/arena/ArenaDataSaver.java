package dev.despical.whackme.arena;

import dev.despical.commons.configuration.ConfigUtils;
import dev.despical.commons.serializer.LocationSerializer;
import dev.despical.whackme.WhackMe;
import dev.despical.whackme.arena.options.ArenaKeys;
import dev.despical.whackme.arena.options.ArenaOption;
import dev.despical.whackme.sign.ArenaSign;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;
import java.util.Set;

/**
 * @author Despical
 * <p>
 * Created at 2.06.2026
 */
public class ArenaDataSaver {

    private final WhackMe plugin;

    public ArenaDataSaver(WhackMe plugin) {
        this.plugin = plugin;
    }

    public void saveAllArenas() {
        ArenaRegistry arenaRegistry = plugin.getArenaRegistry();
        Set<Arena> arenas = arenaRegistry.getArenas();

        FileConfiguration config = arenaRegistry.getConfig();
        for (Arena arena : arenas) {
            saveArenaData(arena, config);
        }

        ConfigUtils.saveConfig(plugin, config, "arenas");
    }

    private void saveArenaData(Arena arena, FileConfiguration config) {
        String rootPath = arena.getId() + ".";

        for (ArenaOption<?> option : ArenaKeys.getPersistentKeys()) {
            saveSingleOption(arena, config, rootPath, option);
        }

        List<String> signLocations = plugin.getSignManager().getSigns(arena)
            .stream()
            .map(ArenaSign::sign)
            .map(Sign::getLocation)
            .map(LocationSerializer::toString)
            .toList();

        config.set(rootPath + "signs", signLocations);
    }

    private <T> void saveSingleOption(Arena arena, FileConfiguration config, String rootPath, ArenaOption<T> option) {
        T value = arena.getOption(option);
        Object serializedValue = option.serialize(value);

        config.set(rootPath + option.getKey(), serializedValue);
    }
}
