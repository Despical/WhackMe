package me.despical.whackme.arena;

import me.despical.commons.configuration.ConfigUtils;
import me.despical.commons.serializer.LocationSerializer;
import me.despical.whackme.WhackMe;
import me.despical.whackme.utils.Utils;
import org.bukkit.entity.Player;

import java.util.HashSet;
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
	private final Set<Arena> arenas;

	public ArenaRegistry(final WhackMe plugin) {
		this.plugin = plugin;
		this.arenas = new HashSet<>();
		this.registerArenas();
	}

	public Set<Arena> getArenas() {
		return Set.copyOf(arenas);
	}

	public boolean isInArena(Player player) {
		return getArena(player) != null;
	}

	public boolean isArena(String id) {
		return getArena(id) != null;
	}

	public Arena getArena(String id) {
		return arenas.stream().filter(arena -> arena.getId().equals(id)).findFirst().orElse(null);
	}

	public Arena getArena(Player player) {
		return arenas.stream().filter(arena -> arena.containPlayer(player)).findFirst().orElse(null);
	}

	public void registerArena(Arena arena) {
		arenas.add(arena);
	}

	public void unregisterArena(Arena arena) {
		arenas.remove(arena);
	}

	public void registerArenas() {
		this.arenas.clear();
		
		final var config = ConfigUtils.getConfig(plugin, "arenas");
		final var section = config.getConfigurationSection("instances");

		if (section == null) {
			plugin.getLogger().warning("Couldn't find 'instance' section in arena.yml, delete the file to regenerate it!");
			return;
		}

		for (var id : section.getKeys(false)) {
			if (id.equals("default")) continue;

			final var path = "instances.%s.".formatted(id);
			final var arena = new Arena(id);
			arena.setReady(true);
			arena.setCustom(config.getBoolean(path + "custom"));
			arena.setStartLocation(LocationSerializer.fromString(config.getString(path + "startLocation")));
			arena.setEndLocation(LocationSerializer.fromString(config.getString(path + "endLocation")));
			arena.setLocations(config.getStringList(path + "portalLocations").stream().map(LocationSerializer::fromString).collect(Collectors.toList()));
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