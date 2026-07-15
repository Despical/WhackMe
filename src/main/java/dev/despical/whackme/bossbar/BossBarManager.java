package dev.despical.whackme.bossbar;

import dev.despical.whackme.WhackMe;
import dev.despical.whackme.arena.Arena;
import dev.despical.whackme.arena.options.ArenaKeys;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

/**
 * Manages the boss bar display for a Whack Me arena.
 * Handles visibility, appearance, and updates.
 *
 * @author Despical
 * <p>
 * Created at 28.01.2026
 */
public class BossBarManager {

    private final Arena arena;
    private final BossBar bossBar;
    private final BossBarConfig configProvider;

    public BossBarManager(Arena arena) {
        this.arena = arena;
        this.bossBar = BossBar.bossBar(Component.empty(), 1F, BossBar.Color.BLUE, BossBar.Overlay.PROGRESS);
        this.configProvider = WhackMe.getInstance().getBossBarConfig();
    }

    public void update() {
        if (!isEnabled()) {
            removePlayer();
            return;
        }

        BossBarConfig.BossBarData barData = configProvider.getData();
        if (barData == null) {
            removePlayer();
            return;
        }

        bossBar.name(barData.title());
        bossBar.color(barData.color());
        bossBar.overlay(barData.overlay());

        if (barData.visible()) {
            addPlayer();
        } else {
            removePlayer();
        }
    }

    public void setProgress(float progress) {
        bossBar.progress(Math.clamp(progress, 0f, 1f));
    }

    public void addPlayer() {
        Player player = arena.getPlayer();
        if (player != null && isEnabled() && shouldShow()) {
            bossBar.addViewer(player);
        }
    }

    public void removePlayer() {
        Player player = arena.getPlayer();
        if (player != null) {
            bossBar.removeViewer(player);
        }
    }

    private boolean shouldShow() {
        BossBarConfig.BossBarData data = configProvider.getData();
        return data != null && data.visible();
    }

    private boolean isEnabled() {
        return configProvider.isEnabled() && arena.getOption(ArenaKeys.ARENA_BOSS_BAR_ENABLED);
    }
}
