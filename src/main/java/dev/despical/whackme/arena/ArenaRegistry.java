package dev.despical.whackme.arena;

import dev.despical.commons.configuration.ConfigUtils;
import dev.despical.commons.serializer.LocationSerializer;
import dev.despical.whackme.WhackMe;
import dev.despical.whackme.util.Utils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * @author Despical
 * <p>
 * Created at 18.06.2022
 */
public class ArenaRegistry {

    private final WhackMe plugin;
    private final Map<String, Arena> arenas;

    public ArenaRegistry(WhackMe plugin) {
        this.plugin = plugin;
        this.arenas = new HashMap<>();

        registerArenas();
    }

    public Set<Arena> getArenas() {
        return new HashSet<>(arenas.values());
    }

    public boolean isInArena(Player player) {
        return getArena(player) != null;
    }

    public boolean isArena(String id) {
        return getArena(id) != null;
    }

    public Arena getArena(String id) {
        return arenas.get(id);
    }

    public Arena getArena(Player player) {
        return arenas.values()
            .stream()
            .filter(arena -> arena.containPlayer(player))
            .findFirst()
            .orElse(null);
    }

    public void registerArena(Arena arena) {
        arenas.put(arena.getId(), arena);
    }

    public void unregisterArena(Arena arena) {
        arenas.remove(arena.getId());
    }

    public void registerArenas() {
        FileConfiguration config = ConfigUtils.getConfig(plugin, "arenas");
        ConfigurationSection section = config.getConfigurationSection("instances");

        if (section == null) {
            plugin.getLogger().warning("Couldn't find 'instance' section in arena.yml, delete the file to regenerate it!");
            return;
        }

        arenas.clear();

        for (String id : section.getKeys(false)) {
            if (id.equals("default")) continue;

            String path = String.format("instances.%s.", id);

            Arena arena = new Arena(id);
            arena.setReady(true);
            arena.setCustom(config.getBoolean(path + "custom"));
            arena.setStartLocation(LocationSerializer.fromString(config.getString(path + "startLocation")));
            arena.setEndLocation(LocationSerializer.fromString(config.getString(path + "endLocation")));
            arena.setLocations(config.getStringList(path + "portalLocations").stream().map(LocationSerializer::fromString).collect(Collectors.toList()));
            arena.setMinimumPoints(config.getInt(path + "minPoints"));
            arena.setMaximumPoints(config.getInt(path + "maxPoints"));
            arena.start();

            registerArena(arena);

            if (!arena.isCustom() && !Utils.isSurroundedBy(arena.getStartLocation())) {
                arena.setReady(false);

                plugin.getLogger().log(Level.WARNING, "Arena ''{0}'' has invalid configuration! (Missing node: INVALID GAME ARENA)", id);
                continue;
            }

            if (arena.isCustom() && arena.getLocations().isEmpty()) {
                arena.setReady(false);

                plugin.getLogger().log(Level.WARNING, "Arena ''{0}'' has invalid configuration! (Missing node: NO PORTALS ADDED)", id);
                continue;
            }

            if (!config.getBoolean(path + "ready")) {
                arena.setReady(false);

                config.set(path + "ready", false);
                ConfigUtils.saveConfig(plugin, config, "arenas");

                plugin.getLogger().log(Level.WARNING, "Setup of arena ''{0}'' is not finished yet!", id);
            }
        }
    }
}
