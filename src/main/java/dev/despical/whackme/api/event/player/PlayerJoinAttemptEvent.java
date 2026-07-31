package dev.despical.whackme.api.event.player;

import dev.despical.whackme.arena.Arena;
import dev.despical.whackme.game.Game;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a player attempts to join a Whack Me game.
 * <p>
 * This event is fired after built-in arena checks pass but before the player is
 * added to the target {@link Game}. Cancelling the event prevents every part
 * of the game entry.
 * <p>
 * Typical use cases include permission checks, queue restrictions, custom
 * cooldowns, maintenance locks, and external matchmaking integrations.
 *
 * @author Despical
 * <p>
 * Created at 18.06.2026
 */
public final class PlayerJoinAttemptEvent extends PlayerEvent implements Cancellable {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    /**
     * The game the player is attempting to join.
     */
    @NotNull
    private final Game game;

    /**
     * Whether this game entry has been cancelled.
     */
    private boolean cancelled;

    /**
     * Constructs a new player join attempt event.
     *
     * @param player the player attempting to join
     * @param game the target game
     */
    public PlayerJoinAttemptEvent(@NotNull Player player, @NotNull Game game) {
        super(player);
        this.game = game;
    }

    /**
     * Returns the game the player is attempting to join.
     *
     * @return the target game
     */
    @NotNull
    public Game getGame() {
        return game;
    }

    /**
     * Returns the arena the player is attempting to join.
     *
     * @return the target arena
     */
    @NotNull
    public Arena getArena() {
        return game.getArena();
    }

    /**
     * Returns whether a listener has denied this join attempt.
     *
     * @return {@code true} when the join is cancelled
     */
    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * Sets whether the player should be prevented from joining.
     *
     * @param cancel {@code true} to deny the join attempt
     */
    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    /**
     * Returns the Bukkit handler list for this event type.
     *
     * @return this event's handler list
     */
    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    /**
     * Returns the static Bukkit handler list for this event type.
     *
     * @return this event's handler list
     */
    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    /**
     * Returns a compact debug representation of the game entry attempt.
     *
     * @return a string containing the player, arena, and cancellation state
     */
    @Override
    public String toString() {
        return "player=%s, arena=%s, cancelled=%s"
            .formatted(getPlayer().getName(), getArena().getId(), cancelled);
    }
}
