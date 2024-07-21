package me.despical.whackme.arena.blocks;

import me.despical.commons.string.StringFormatUtils;
import me.despical.whackme.WhackMe;
import me.despical.whackme.api.StatsStorage;
import me.despical.whackme.arena.Arena;
import me.despical.whackme.utils.ActionBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Despical
 * <p>
 * Created at 21.06.2022
 */
public class PointHandler extends BukkitRunnable {

	private final Arena arena;
	private final WhackMe plugin;

	public PointHandler(Arena arena, WhackMe plugin) {
		this.arena = arena;
		this.plugin = plugin;
	}

	@Override
	public void run() {
		final Player player = arena.getPlayer();

		if (player == null) return;

		sendActionBar(player);

		int size = arena.getPointBlocks().size();
		int maximumPoints = arena.getMaximumPoints();

		if (size <= maximumPoints && size < random(maximumPoints + 1)) {
			new PointBlock(arena).handleItself();
		}
	}

	private int random(int max) {
		int min = arena.getMinimumPoints();

		return min == max ? min : ThreadLocalRandom.current().nextInt(min, max);
	}

	private void sendActionBar(final Player player) {
		String message = plugin.getChatManager().message("in_game.action_bar");

		message = message.replace("%player%", player.getName());
		message = message.replace("%score%", Integer.toString(StatsStorage.getUserStats(player, StatsStorage.StatisticType.LOCAL_SCORE)));
		message = message.replace("%timer%", StringFormatUtils.formatIntoMMSS(arena.getTimer()));

		ActionBar.sendActionBar(player, message);
	}

	public void handleTask() {
		runTaskTimer(plugin, 8L, plugin.getConfigPreferences().getTicks());
	}
}