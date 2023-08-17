package me.despical.whackme.user;

import me.despical.whackme.WhackMe;
import me.despical.whackme.api.StatsStorage;
import me.despical.whackme.api.event.player.WMPlayerStatisticChangeEvent;
import me.despical.whackme.arena.Arena;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Despical
 * <p>
 * Created at 20.06.2022
 */
public class User {

	private static final WhackMe plugin = JavaPlugin.getPlugin(WhackMe.class);

	private final UUID uuid;
	private final Player player;
	private final Map<StatsStorage.StatisticType, Integer> stats;

	public User(Player player) {
		this.player = player;
		this.uuid = player.getUniqueId();
		this.stats = new EnumMap<>(StatsStorage.StatisticType.class);
	}

	public Arena getArena() {
		return plugin.getArenaRegistry().getArena(player);
	}

	public Player getPlayer() {
		return player;
	}

	public UUID getUniqueId() {
		return uuid;
	}

	public Object getName() {
		return player.getName();
	}

	public int getStat(StatsStorage.StatisticType statisticType) {
		final var statistic = stats.get(statisticType);

		if (statistic == null) {
			stats.put(statisticType, 0);
			return 0;
		}

		return statistic;
	}

	public void setStat(StatsStorage.StatisticType stat, int value) {
		stats.put(stat, value);

		// When disable initialized you can no longer create a scheduler
		if (plugin.isEnabled()) plugin.getServer().getScheduler().runTask(plugin, () -> plugin.getServer().getPluginManager().callEvent(new WMPlayerStatisticChangeEvent(getArena(), player, stat, value)));
	}

	public void addStat(StatsStorage.StatisticType stat, int value) {
		setStat(stat, getStat(stat) + value);
	}
}