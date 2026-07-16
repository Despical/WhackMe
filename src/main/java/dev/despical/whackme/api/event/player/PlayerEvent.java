package dev.despical.whackme.api.event.player;

import dev.despical.whackme.WhackMe;
import dev.despical.whackme.api.event.WhackMeEvent;
import dev.despical.whackme.user.User;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Base class for Whack Me events associated with one Bukkit player.
 * <p>
 * {@link #getPlayer()} provides the live Bukkit entity, while
 * {@link #getUser()} resolves the plugin-owned user model used for statistics
 * and game membership.
 * <p>
 * API Note: The player may disconnect between event dispatch and delayed work.
 * Do not retain the Bukkit entity for asynchronous or long-lived operations;
 * retain its UUID instead.
 * @author Despical
 * <p>
 * Created at 29.01.2026
 */
public abstract class PlayerEvent extends WhackMeEvent {

    @NotNull
    private final Player player;

    /**
     * Creates a player-scoped Whack Me event.
     *
     * @param player Bukkit player associated with the event
     */
    protected PlayerEvent(@NotNull Player player) {
        this.player = player;
    }

    /**
     * Returns the Bukkit player associated with this event.
     *
     * @return event player
     */
    @NotNull
    public final Player getPlayer() {
        return player;
    }

    /**
     * Resolves the Whack Me user model associated with the event player.
     *
     * @return plugin user model for the player
     */
    @NotNull
    public final User getUser() {
        return WhackMe.getInstance().getUserManager().getUser(player);
    }

    /**
     * Returns a compact representation suitable for profiler output.
     *
     * @return player detail string
     */
    @Override
    public String toString() {
        return "[player=%s]".formatted(player.getName());
    }
}
