package me.despical.whackme.handlers;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.despical.whackme.WhackMe;
import me.despical.whackme.arena.Arena;
import me.despical.whackme.user.User;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static me.despical.whackme.api.StatsStorage.StatisticType.*;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2022
 */
public class PlaceholderManager extends PlaceholderExpansion {

	private final WhackMe plugin;

	public PlaceholderManager(WhackMe plugin) {
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

		final User user = plugin.getUserManager().getUser(player);

		switch (id.toLowerCase()) {
			case "online_players":
				return Long.toString(plugin.getArenaRegistry().getArenas().stream().filter(arena -> arena.getPlayer() != null).count());
			case "whacked_point_blocks":
				return Integer.toString(user.getStat(PLUS_BLOCKS));
			case "whacked_minus_point_blocks":
				return Integer.toString(user.getStat(MINUS_BLOCKS));
			case "whacked_block_rate":
				int minusBlocks = user.getStat(MINUS_BLOCKS), plusBlocks = user.getStat(PLUS_BLOCKS);
				return String.format("%.1f", (minusBlocks + plusBlocks == 0 ? 100 : ((double) plusBlocks / (minusBlocks + plusBlocks)) * 100D));
			case "record_score":
				return Integer.toString(user.getStat(RECORD_SCORE));
			case "tours_played":
				return Integer.toString(user.getStat(TOURS_PLAYED));
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
				return arena.getPlayerName();
			case "point_blocks":
				return Integer.toString(arena.getPointBlocks().size());
			default:
				return null;
		}
	}
}