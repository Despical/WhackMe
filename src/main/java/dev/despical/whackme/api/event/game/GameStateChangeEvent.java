package dev.despical.whackme.api.event.game;

import dev.despical.whackme.game.Game;
import dev.despical.whackme.game.GameState;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called immediately before a Whack Me game changes state.
 * <p>
 * The game still reports {@link #getOldState()} during dispatch. Cancelling the
 * event prevents assignment of {@link #getNewState()} and prevents the target
 * state's first tick from running.
 * <p>
 * Cancelling internal recovery transitions such as
 * {@code RESTARTING} may leave a game occupied. Listeners should only cancel a
 * transition when they also manage the resulting lifecycle.
 *
 * @author Despical
 * <p>
 * Created at 18.06.2026
 */
public final class GameStateChangeEvent extends GameEvent implements Cancellable {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private boolean cancelled;

    /**
     * The state assigned when this event was created.
     */
    @NotNull
    private final GameState oldState;

    /**
     * The requested destination state.
     */
    @NotNull
    private final GameState newState;

    /**
     * Constructs a new game state change event.
     *
     * @param game the game whose state is changing
     * @param oldState the currently assigned state
     * @param newState the requested destination state
     */
    public GameStateChangeEvent(@NotNull Game game, @NotNull GameState oldState, @NotNull GameState newState) {
        super(game);
        this.oldState = oldState;
        this.newState = newState;
    }

    /**
     * Returns the state assigned when the event was fired.
     *
     * @return the current state before the transition
     */
    @NotNull
    public GameState getOldState() {
        return oldState;
    }

    /**
     * Returns the requested destination state.
     *
     * @return the state that will be assigned unless cancelled
     */
    @NotNull
    public GameState getNewState() {
        return newState;
    }

    /**
     * Returns whether the state transition has been cancelled.
     *
     * @return {@code true} when the old state must be retained
     */
    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * Sets whether the requested transition should be prevented.
     *
     * @param cancel {@code true} to retain the old state
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
     * Returns a compact debug representation of the requested state change.
     *
     * @return a string containing the arena, states, and cancellation state
     */
    @Override
    public String toString() {
        return "arena=%s, oldState=%s, newState=%s, cancelled=%s"
            .formatted(getArena().getId(), oldState, newState, cancelled);
    }
}
