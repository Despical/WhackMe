package me.despical.whackme.handlers;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.despical.whackme.Main;
import me.despical.whackme.api.StatsStorage;
import me.despical.whackme.arena.Arena;
import me.despical.whackme.arena.ArenaRegistry;
import me.despical.whackme.user.User;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2022
 */
public class PlaceholderManager extends PlaceholderExpansion {

	private final Main plugin;

	public PlaceholderManager(Main plugin) {
		this.plugin = plugin;

		register();
	}

	@NotNull
	@Override
	public boolean persist() {
		return true;
	}

	@NotNull
	@Override
	public String getIdentifier() {
		return "wm";
	}

	@NotNull
	@Override
	public String getAuthor() {
		return "Despical";
	}

	@NotNull
	@Override
	public String getVersion() {
		return plugin.getDescription().getVersion();
	}

	@Override
	public String onPlaceholderRequest(Player player, String id) {
		if (player == null) return null;

		final User user = plugin.getUserManager().getUser(player);

		switch (id.toLowerCase()) {
			case "record_score":
				return Integer.toString(user.getStat(StatsStorage.StatisticType.RECORD_SCORE));
			case "tours_played":
				return Integer.toString(user.getStat(StatsStorage.StatisticType.TOURS_PLAYED));
			default:
				return handleArenaPlaceholderRequest(id);
		}
	}

	private String handleArenaPlaceholderRequest(String id) {
		final String[] data = id.split(":");
		final Arena arena = plugin.getArenaRegistry().getArena(data[0]);

		if (arena == null) return null;

		switch (data[1].toLowerCase()) {
			case "player_name":
				return arena.getPlayer() == null ? "Unknown" : arena.getPlayer().getName();
			case "point_blocks":
				return Integer.toString(arena.getPointBlocks().size());
			default:
				return null;
		}
	}
}