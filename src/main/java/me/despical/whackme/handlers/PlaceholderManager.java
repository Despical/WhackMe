package me.despical.whackme.handlers;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.despical.whackme.Main;
import me.despical.whackme.api.StatsStorage;
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
	public String onPlaceholderRequest(Player player, @NotNull String id) {
		if (player == null) return null;

		final var user = plugin.getUserManager().getUser(player);

		return switch (id.toLowerCase()) {
			case "record_score" -> Integer.toString(user.getStat(StatsStorage.StatisticType.RECORD_SCORE));
			case "tours_played" -> Integer.toString(user.getStat(StatsStorage.StatisticType.TOURS_PLAYED));
			default -> handleArenaPlaceholderRequest(id);
		};
	}

	private String handleArenaPlaceholderRequest(String id) {
		final var data = id.split(":");
		final var arena = plugin.getArenaRegistry().getArena(data[0]);

		if (arena == null) return null;

		return switch (data[1].toLowerCase()) {
			case "player_name" -> arena.getPlayer() == null ? "Unknown" : arena.getPlayer().getName();
			case "point_blocks" -> Integer.toString(arena.getPointBlocks().size());
			default -> null;
		};
	}
}