package dev.despical.whackme.arena.blocks;

import dev.despical.whackme.util.Schedulers;
import org.bukkit.scheduler.BukkitTask;

/**
 * @author Despical
 * <p>
 * Created at 14.07.2026
 */
final class PointBlockMovement implements Runnable {

    private static final double UPPER_TRAVEL_LIMIT = 0.85;
    private static final double LOWER_TRAVEL_LIMIT = -0.6;

    private final PointBlockDisplay display;
    private final PointBlockSettings settings;
    private final Runnable completion;

    private BukkitTask task;
    private Phase phase = Phase.RISING;
    private double verticalOffset;
    private int remainingWaitTicks;

    PointBlockMovement(PointBlockDisplay display, PointBlockSettings settings, Runnable completion) {
        this.display = display;
        this.settings = settings;
        this.completion = completion;
        this.remainingWaitTicks = settings.normalizedWaitTicks();
    }

    void start() {
        if (task != null) {
            return;
        }

        if (settings.runAsync()) {
            task = Schedulers.runTaskTimerAsynchronously(this, 1L, 1L);
            return;
        }

        task = Schedulers.runTaskTimer(this, 1L, 1L);
    }

    void stop() {
        if (task == null) {
            return;
        }

        task.cancel();
        task = null;
    }

    @Override
    public void run() {
        switch (phase) {
            case RISING -> rise();
            case WAITING -> waitAtPeak();
            case FALLING -> fall();
        }
    }

    private void rise() {
        double step = settings.verticalStep();
        verticalOffset += step;

        if (verticalOffset > UPPER_TRAVEL_LIMIT) {
            phase = Phase.WAITING;
            return;
        }

        display.moveVertically(step);
    }

    private void waitAtPeak() {
        if (remainingWaitTicks > 0) {
            remainingWaitTicks--;

            if (remainingWaitTicks > 0) {
                return;
            }
        }

        phase = Phase.FALLING;
        fall();
    }

    private void fall() {
        double step = settings.verticalStep();
        verticalOffset -= step;

        if (verticalOffset < LOWER_TRAVEL_LIMIT) {
            stop();
            completion.run();
            return;
        }

        display.moveVertically(-step);
    }

    private enum Phase {
        RISING,
        WAITING,
        FALLING
    }
}
