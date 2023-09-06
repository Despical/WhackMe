package me.despical.whackme.arena.managers;

import me.despical.commons.ReflectionUtils;
import me.despical.commons.number.NumberUtils;
import me.despical.whackme.ConfigPreferences;
import me.despical.whackme.WhackMe;
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

	private final WhackMe plugin;
	private final Arena arena;
	private final boolean enabled;

	private BossBar bossBar;
	private List<String> messages;

	private int queue = 0;

	public BossBarManager(WhackMe plugin, Arena arena) {
		this.plugin = plugin;
		this.arena = arena;
		this.enabled = plugin.getOption(ConfigPreferences.Option.BOSS_BAR_ENABLED) && ReflectionUtils.supports(13);

		if (enabled) {
			final var chatManager = plugin.getChatManager();

			this.bossBar = plugin.getServer().createBossBar(chatManager.message("boss_bar.game_info"), BarColor.valueOf(chatManager.message("boss_bar.color")), BarStyle.valueOf(chatManager.message("boss_bar.style")));
			this.messages = chatManager.getStringList("boss_bar.messages");
			this.runTaskTimer(plugin, 20, NumberUtils.getInt(chatManager.message("boss_bar.interval"), 300));
		}
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

	@Override
	public void run() {
		if (queue + 1 > messages.size()) queue = 0;

		this.bossBar.setTitle(plugin.getChatManager().coloredRawMessage(messages.get(queue++)));
	}
}