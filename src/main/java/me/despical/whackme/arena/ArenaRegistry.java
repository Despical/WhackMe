package me.despical.whackme.arena;

import me.despical.commons.configuration.ConfigUtils;
import me.despical.commons.serializer.LocationSerializer;
import me.despical.whackme.Main;
import me.despical.whackme.utils.Utils;
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
		
		final var config = ConfigUtils.getConfig(plugin, "arenas");
		final var chatManager = plugin.getChatManager();
		final var logger = plugin.getLogger();

		if (!config.contains("instances")) {
			logger.info(chatManager.message("validator.no_instances_created"));
			return;
		}

		final var section = config.getConfigurationSection("instances");

		if (section == null) {
			logger.info(chatManager.message("validator.no_instances_created"));
			return;
		}

		for (var id : section.getKeys(false)) {
			final var path = "instances.%s.".formatted(id);

			if (path.contains("default")) continue;

			final var arena = new Arena(id);
			arena.setReady(true);
			arena.setStartLocation(LocationSerializer.fromString(config.getString(path + "startLocation")));
			arena.setEndLocation(LocationSerializer.fromString(config.getString(path + "endLocation")));
			arena.start();

			registerArena(arena);

			if (!Utils.isSurroundedBy(arena.getStartLocation())) {
				arena.setReady(false);

				logger.info(chatManager.message("validator.invalid_arena_configuration").replace("%arena%", id).replace("%error%", "INVALID GAME AREA"));
				continue;
			}

			if (!config.getBoolean(path + "ready")) {
				arena.setReady(false);

				config.set(path + "ready", false);
				ConfigUtils.saveConfig(plugin, config, "arenas");

				logger.info(chatManager.message("validator.invalid_arena_configuration").replace("%arena%", id).replace("%error%", "NOT VALIDATED"));
				continue;
			}

			ConfigUtils.saveConfig(plugin, config, "arenas");
			logger.info(chatManager.message("validator.instance_started").replace("%arena%", id));
		}
	}
}