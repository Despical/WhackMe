package dev.despical.whackme.arena.managers;

import dev.despical.commons.number.NumberUtils;
import dev.despical.commons.reflection.XReflection;
import dev.despical.whackme.ConfigPreferences;
import dev.despical.whackme.WhackMe;
import dev.despical.whackme.arena.Arena;
import dev.despical.whackme.handler.ChatManager;
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

    private final Arena arena;
    private final WhackMe plugin;
    private final boolean enabled;
    private int queue = 0;
    private BossBar bossBar;
    private List<String> messages;

    public BossBarManager(Arena arena) {
        this.arena = arena;
        this.plugin = WhackMe.getInstance();
        this.enabled = plugin.getOption(ConfigPreferences.Option.BOSS_BAR_ENABLED) && XReflection.supports(13);

        if (enabled) {
            ChatManager chatManager = plugin.getChatManager();

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
