package me.despical.whackme.arena.managers;

import me.despical.commons.number.NumberUtils;
import me.despical.whackme.ConfigPreferences;
import me.despical.whackme.Main;
import me.despical.whackme.arena.Arena;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2022
 */
public class BossBarManager extends BukkitRunnable {

	private final Main plugin;
	private final Arena arena;
	private final BossBar bossBar;
	private final boolean enabled;
	private final List<String> messages;

	private int queue = 0;

	public BossBarManager(Main plugin, Arena arena) {
		this.plugin = plugin;
		this.arena = arena;
		this.enabled = plugin.getConfigPreferences().getOption(ConfigPreferences.Option.BOSS_BAR_ENABLED);
		this.bossBar = plugin.getServer().createBossBar(plugin.getChatManager().message("boss_bar.game_info"), BarColor.valueOf(plugin.getChatManager().message("boss_bar.color")), BarStyle.valueOf(plugin.getChatManager().message("boss_bar.style")));
		this.messages = plugin.getChatManager().getStringList("boss_bar.messages");

		if (enabled) this.runTaskTimer(plugin, 20, NumberUtils.getInt(plugin.getChatManager().message("boss_bar.interval"), 300));
	}

	public void addPlayer() {
		if (!enabled) return;
		if (arena.getPlayer() == null) return;

		this.bossBar.addPlayer(arena.getPlayer());
	}

	public void removePlayer() {
		if (!enabled) return;
		if (arena.getPlayer() == null) return;

		this.bossBar.removePlayer(arena.getPlayer());
	}

	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public void run() {
		if (queue + 1 > messages.size()) queue = 0;

		this.bossBar.setTitle(plugin.getChatManager().coloredRawMessage(messages.get(queue++)));
	}
}