package me.despical.whackme.arena.blocks;

import me.despical.commons.string.StringFormatUtils;
import me.despical.whackme.Main;
import me.despical.whackme.api.StatsStorage;
import me.despical.whackme.arena.Arena;
import me.despical.whackme.util.ActionBar;
import org.apache.commons.lang.StringUtils;
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

		sendActionBar();

		int size = arena.getPointBlocks().size();

		if (size <= arena.getMaximumPoints() && size < ThreadLocalRandom.current().nextInt(arena.getMinimumPoints(), arena.getMaximumPoints())) {
			new PointBlock(arena).runTaskTimer(plugin, 1L, 1L);
		}
	}

	private void sendActionBar() {
		Player player = arena.getPlayer();
		String message = plugin.getChatManager().message("in_game.action_bar");
		message = StringUtils.replace(message, "%player%", player.getName());
		message = StringUtils.replace(message, "%score%", Integer.toString(StatsStorage.getUserStats(player, StatsStorage.StatisticType.LOCAL_SCORE)));
		message = StringUtils.replace(message, "%timer%", StringFormatUtils.formatIntoMMSS(arena.getTimer()));

		ActionBar.sendActionBar(player, message);
	}

	public void handleTask() {
		runTaskTimer(plugin, 8L, 8L);
	}
}