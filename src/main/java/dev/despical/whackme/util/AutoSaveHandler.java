package dev.despical.whackme.util;

import dev.despical.whackme.WhackMe;
import lombok.RequiredArgsConstructor;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * @author Despical
 * <p>
 * Created at 02.06.2026
 */
@RequiredArgsConstructor
public final class AutoSaveHandler extends BukkitRunnable {

    private final WhackMe plugin;

    @Override
    public void run() {
        plugin.getDatabase().saveAllData();
        plugin.getArenaDataSaver().saveAllArenas();
        plugin.getLeaderboardManager().refreshAllLeaderboards();
    }
}
