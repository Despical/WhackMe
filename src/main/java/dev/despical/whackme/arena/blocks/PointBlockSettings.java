package dev.despical.whackme.arena.blocks;

import dev.despical.whackme.arena.Arena;
import dev.despical.whackme.arena.options.ArenaKeys;

/**
 * @author Despical
 * <p>
 * Created at 14.07.2026
 */
record PointBlockSettings(boolean runAsync, double yMultiplier, double maxYMultiplier, int waitTicks) {

    static PointBlockSettings from(Arena arena) {
        return new PointBlockSettings(
            arena.getOption(ArenaKeys.POINT_BLOCKS_RUN_ASYNC),
            arena.getOption(ArenaKeys.POINT_BLOCK_Y_MULTIPLIER),
            arena.getOption(ArenaKeys.POINT_BLOCK_MAX_Y_MULTIPLIER),
            arena.getOption(ArenaKeys.POINT_BLOCK_WAIT_TICKS)
        );
    }

    double verticalStep() {
        return Math.max(0.01, Math.min(yMultiplier, maxYMultiplier));
    }

    int normalizedWaitTicks() {
        return Math.max(0, waitTicks);
    }
}
