package me.despical.whackme.arena.blocks;

import me.despical.commons.string.StringFormatUtils;
import me.despical.whackme.Main;
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

	private final Main plugin;
	private final Arena arena;

	public PointHandler(Main plugin, Arena arena) {
		this.plugin = plugin;
		this.arena = arena;
	}

	@Override
	public void run() {
		Player player = arena.getPlayer();

		if (player == null) return;

		sendActionBar(player);

		int size = arena.getPointBlocks().size();

		if (size <= arena.getMaximumPoints() && size < ThreadLocalRandom.current().nextInt(arena.getMinimumPoints(), arena.getMaximumPoints())) {
			new PointBlock(arena).handleItself();
		}
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