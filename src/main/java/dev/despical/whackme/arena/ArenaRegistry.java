package dev.despical.whackme.arena;

import dev.despical.commons.configuration.ConfigUtils;
import dev.despical.whackme.WhackMe;
import dev.despical.whackme.arena.options.ArenaKeys;
import dev.despical.whackme.arena.options.ArenaOption;
import dev.despical.whackme.user.User;
import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * @author Despical
 * <p>
 * Created at 18.06.2022
 */
public class ArenaRegistry {

    private final WhackMe plugin;
    @Getter
    private final FileConfiguration config;
    private final Map<String, Arena> arenas;

    public ArenaRegistry(WhackMe plugin) {
        this.plugin = plugin;
        this.config = ConfigUtils.getConfig(plugin, "arenas");
        this.arenas = new HashMap<>();
        this.registerArenas();
    }

    public Arena getArena(User user) {
        return arenas.values()
            .stream()
            .filter(arena -> arena.isPlaying(user))
            .findFirst()
            .orElse(null);
    }

    public Arena getArena(Player player) {
        User user = plugin.getUserManager().getUser(player);
        return getArena(user);
    }

    public boolean isInArena(Player player) {
        return getArena(player) != null;
    }

    public Arena getArena(String id) {
        return findArena(id).orElse(null);
    }

    public Optional<Arena> findArena(String id) {
        return Optional.ofNullable(arenas.get(id));
    }

    public Optional<Arena> findArena(Player player) {
        return Optional.ofNullable(getArena(player));
    }

    public boolean isArenaExists(String id) {
        return arenas.containsKey(id);
    }

    public void registerNewArena(String id) {
        arenas.put(id, new Arena(id));
    }

    public void unregisterArena(Arena arena) {
        arena.stop();
        arenas.remove(arena.getId());

        plugin.getSignManager().removeArenaSigns(arena);

        config.set(arena.getId(), null);
    }

    public Set<Arena> getArenas() {
        return Set.copyOf(arenas.values());
    }

    public Set<String> getArenaNames() {
        return arenas.keySet();
    }

    public void registerArenas() {
        arenas.clear();

        for (String id : config.getKeys(false)) {
            Arena arena = new Arena(id);
            loadOptionsFor(arena, config);
            validateReadyState(arena);

            if (arena.getOption(ArenaKeys.READY)) {
                arena.start();
            }

            arenas.put(id, arena);
        }
    }

    private void loadOptionsFor(Arena arena, FileConfiguration config) {
        for (ArenaOption<?> option : ArenaKeys.getPersistentKeys()) {
            loadSingleOption(arena, config, option);
        }
    }

    private <T> void loadSingleOption(Arena arena, FileConfiguration config, ArenaOption<T> option) {
        String path = "%s.%s".formatted(arena.getId(), option.getKey());

        if (config.contains(path)) {
            Object rawValue = config.get(path);
            T value = option.deserialize(rawValue);

            arena.setOption(option, value);
        } else {
            arena.setOption(option, cloneDefaultValue(option.getDefaultValue()));
        }
    }

    private void validateReadyState(Arena arena) {
        if (!arena.getOption(ArenaKeys.READY)) {
            return;
        }

        boolean missingRequiredLocation = arena.getOption(ArenaKeys.START_LOCATION) == null
            || arena.getOption(ArenaKeys.END_LOCATION) == null
            || arena.getOption(ArenaKeys.PORTAL_LOCATIONS).isEmpty();

        if (!missingRequiredLocation) {
            return;
        }

        arena.setOption(ArenaKeys.READY, false);
        config.set(arena.getId() + "." + ArenaKeys.READY.getKey(), false);
    }

    @SuppressWarnings("unchecked")
    private <T> T cloneDefaultValue(T value) {
        if (value instanceof java.util.List<?> list) {
            return (T) new java.util.ArrayList<>(list);
        }

        return value;
    }
}
