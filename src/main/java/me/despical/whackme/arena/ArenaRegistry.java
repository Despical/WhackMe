package me.despical.whackme.arena;

import me.despical.commons.configuration.ConfigUtils;
import me.despical.commons.serializer.LocationSerializer;
import me.despical.whackme.Main;
import me.despical.whackme.utils.Utils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Despical
 * <p>
 * Created at 18.06.2022
 */
public class ArenaRegistry {

	private final Main plugin;
	private final Set<Arena> arenas;

	public ArenaRegistry(final Main plugin) {
		this.plugin = plugin;
		this.arenas = new HashSet<>();
		this.registerArenas();
	}

	public Set<Arena> getArenas() {
		return new HashSet<>(arenas);
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
		arenas.clear();
		
		final FileConfiguration config = ConfigUtils.getConfig(plugin, "arenas");

		if (!config.contains("instances")) {
			plugin.getLogger().info(plugin.getChatManager().message("validator.no_instances_created"));
			return;
		}

		ConfigurationSection section = config.getConfigurationSection("instances");

		if (section == null) {
			plugin.getLogger().info(plugin.getChatManager().message("validator.no_instances_created"));
			return;
		}

		for (String id : section.getKeys(false)) {
			final String path = "instances." + id + ".";

			if (path.contains("default")) continue;

			final Arena arena = new Arena(id);
			arena.setReady(true);
			arena.setStartLocation(LocationSerializer.fromString(config.getString(path + "startLocation")));
			arena.setEndLocation(LocationSerializer.fromString(config.getString(path + "endLocation")));
			arena.start();

			registerArena(arena);

			if (!Utils.isSurroundedBy(arena.getStartLocation())) {
				plugin.getLogger().info(plugin.getChatManager().message("validator.invalid_arena_configuration").replace("%arena%", id).replace("%error%", "INVALID GAME AREA"));

				arena.setReady(false);

				config.set(path + "ready", false);
				ConfigUtils.saveConfig(plugin, config, "arenas");
				continue;
			}

			if (!config.getBoolean(path + "ready")) {
				arena.setReady(false);

				config.set(path + "ready", false);
				ConfigUtils.saveConfig(plugin, config, "arenas");

				plugin.getLogger().info(plugin.getChatManager().message("validator.invalid_arena_configuration").replace("%arena%", id).replace("%error%", "NOT VALIDATED"));
				continue;
			}

			ConfigUtils.saveConfig(plugin, config, "arenas");
			plugin.getLogger().info(plugin.getChatManager().message("validator.instance_started").replace("%arena%", id));
		}
	}
}