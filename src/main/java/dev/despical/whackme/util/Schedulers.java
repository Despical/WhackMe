package dev.despical.whackme.util;

import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitScheduler;

import dev.despical.whackme.WhackMe;

/**
 * @author Despical
 * <p>
 * Created at 04.06.2026
 */
@UtilityClass
public final class Schedulers {

    private static final WhackMe plugin = WhackMe.getInstance();
    private static final BukkitScheduler scheduler = Bukkit.getScheduler();

    public static void runInTheNextTick(Runnable runnable) {
        scheduler.runTask(plugin, runnable);
    }

    public static void runTaskLater(Runnable runnable, long delay) {
        scheduler.runTaskLater(plugin, runnable, delay);
    }

    public static void runTaskAsynchronously(Runnable runnable) {
        scheduler.runTaskAsynchronously(plugin, runnable);
    }
}
