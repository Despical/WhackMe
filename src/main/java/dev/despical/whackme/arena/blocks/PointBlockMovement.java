package dev.despical.whackme.arena.blocks;

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
    private final PointBlockTicker ticker;

    private volatile boolean running;
    private Phase phase = Phase.RISING;
    private double verticalOffset;
    private int remainingWaitTicks;

    PointBlockMovement(PointBlockDisplay display, PointBlockSettings settings, PointBlockTicker ticker, Runnable completion) {
        this.display = display;
        this.settings = settings;
        this.ticker = ticker;
        this.completion = completion;
        this.remainingWaitTicks = settings.normalizedWaitTicks();
    }

    void start() {
        if (running) {
            return;
        }

        running = true;
        ticker.add(this, settings.runAsync());
    }

    void stop() {
        if (!running) {
            return;
        }

        running = false;
        ticker.remove(this);
    }

    @Override
    public void run() {
        if (!running) {
            return;
        }

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
