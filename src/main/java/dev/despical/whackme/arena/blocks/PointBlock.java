package dev.despical.whackme.arena.blocks;

import dev.despical.whackme.WhackMe;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Coordinates the lifecycle of one point block. Movement, presentation and
 * player interaction are delegated to dedicated collaborators.
 *
 * @author Despical
 * <p>
 * Created at 20.06.2022
 */
public final class PointBlock {

    private final WhackMe plugin;
    private final PointHandler pointHandler;
    private final Location portalLocation;
    private final PointBlockDisplay display;
    private final PointBlockMovement movement;
    private final PointBlockInteractionListener interactionListener;
    private final AtomicBoolean cleared;

    PointBlock(
        WhackMe plugin,
        PointHandler pointHandler,
        Location portalLocation,
        PointBlockDisplay display,
        PointBlockSettings settings,
        PointBlockScoreService scoreService
    ) {
        this.plugin = plugin;
        this.pointHandler = pointHandler;
        this.portalLocation = portalLocation;
        this.display = display;
        this.movement = new PointBlockMovement(display, settings, this::finishOnMainThread);
        this.interactionListener = new PointBlockInteractionListener(plugin, pointHandler, display, scoreService);
        this.cleared = new AtomicBoolean();
    }

    public void start() {
        interactionListener.register();
        movement.start();
    }

    public void clear() {
        if (!Bukkit.isPrimaryThread()) {
            plugin.getServer().getScheduler().runTask(plugin, this::clear);
            return;
        }

        cleanUp();
    }

    public PointBlockType getType() {
        return display.getType();
    }

    private void finishOnMainThread() {
        if (Bukkit.isPrimaryThread()) {
            cleanUp();
        } else {
            plugin.getServer().getScheduler().runTask(plugin, this::cleanUp);
        }
    }

    private void cleanUp() {
        if (!cleared.compareAndSet(false, true)) {
            return;
        }

        movement.stop();
        interactionListener.unregister();
        display.remove();
        pointHandler.release(this, portalLocation);
    }
}
