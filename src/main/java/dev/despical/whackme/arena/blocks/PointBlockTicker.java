package dev.despical.whackme.arena.blocks;

import dev.despical.whackme.util.Schedulers;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Moves every rising and falling point block of one arena from a single repeating task, instead of
 * starting a separate one-tick timer for each block. The task starts when the first block is added and
 * stops once no block is moving.
 *
 * @author Despical
 * <p>
 * Created at 09.11.2026
 */
final class PointBlockTicker implements Runnable {

    private final List<PointBlockMovement> movements = new CopyOnWriteArrayList<>();

    private BukkitTask task;

    synchronized void add(PointBlockMovement movement, boolean runAsync) {
        movements.add(movement);

        if (task == null) {
            task = runAsync
                ? Schedulers.runTaskTimerAsynchronously(this, 1L, 1L)
                : Schedulers.runTaskTimer(this, 1L, 1L);
        }
    }

    synchronized void remove(PointBlockMovement movement) {
        movements.remove(movement);

        if (movements.isEmpty() && task != null) {
            task.cancel();
            task = null;
        }
    }

    @Override
    public void run() {
        // Copy-on-write iteration, so a block may finish and remove itself during this loop.
        for (PointBlockMovement movement : movements) {
            movement.run();
        }
    }
}
