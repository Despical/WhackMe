package me.despical.whackme.arena;

import me.despical.commons.configuration.ConfigUtils;
import me.despical.commons.serializer.LocationSerializer;
import me.despical.commons.util.LogUtils;
import me.despical.whackme.Main;
import me.despical.whackme.util.Utils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Despical
 * <p>
 * Created at 18.06.2022
 */
public class ArenaRegistry {

	private static final Main plugin = JavaPlugin.getPlugin(Main.class);
	private static final Set<Arena> arenas = new HashSet<>();

	public static Set<Arena> getArenas() {
		return new HashSet<>(arenas);
	}

	public static boolean isInArena(Player player) {
		return getArena(player) != null;
	}

	public static boolean isArena(String id) {
		return getArena(id) != null;
	}

	public static Arena getArena(String id) {
		return arenas.stream().filter(arena -> arena.getId().equals(id)).findFirst().orElse(null);
	}

	public static Arena getArena(Player player) {
		return arenas.stream().filter(arena -> arena.containPlayer(player)).findFirst().orElse(null);
	}

	public static void registerArena(Arena arena) {
		arenas.add(arena);
	}

	public static void unregisterArena(Arena arena) {
		arenas.remove(arena);
	}

	public static void registerArenas() {
		LogUtils.log("Arena registration started.");

		final FileConfiguration config = ConfigUtils.getConfig(plugin, "arenas");
		final long start = System.currentTimeMillis();

		arenas.clear();

		if (!config.contains("instances")) {
			LogUtils.sendConsoleMessage(plugin.getChatManager().message("validator.no_instances_created"));
			return;
		}

		ConfigurationSection section = config.getConfigurationSection("instances");

		if (section == null) {
			LogUtils.sendConsoleMessage(plugin.getChatManager().message("validator.no_instances_created"));
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
				LogUtils.sendConsoleMessage(plugin.getChatManager().message("validator.invalid_arena_configuration").replace("%arena%", id).replace("%error%", "INVALID GAME AREA"));

				arena.setReady(false);

				config.set(path + "ready", false);
				ConfigUtils.saveConfig(plugin, config, "arenas");
				continue;
			}

			if (!config.getBoolean(path + "ready")) {
				arena.setReady(false);

				config.set(path + "ready", false);
				ConfigUtils.saveConfig(plugin, config, "arenas");

				LogUtils.sendConsoleMessage(plugin.getChatManager().message("validator.invalid_arena_configuration").replace("%arena%", id).replace("%error%", "NOT VALIDATED"));
				continue;
			}

			ConfigUtils.saveConfig(plugin, config, "arenas");
			LogUtils.sendConsoleMessage(plugin.getChatManager().message("validator.instance_started").replace("%arena%", id));
		}

		LogUtils.log("Arenas registration completed, took {0} ms.", System.currentTimeMillis() - start);
	}
}