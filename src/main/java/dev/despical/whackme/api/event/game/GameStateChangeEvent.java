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
 *
 * <pre>{@code
 * @EventHandler
 * public void onStateChange(GameStateChangeEvent event) {
 *     if (event.getNewState() == GameState.IN_GAME && maintenanceMode) {
 *         event.setCancelled(true);
 *     }
 * }
 * }</pre>
 *
 * @apiNote Cancelling internal recovery transitions such as
 * {@code RESTARTING} may leave a game occupied. Listeners should only cancel a
 * transition when they also manage the resulting lifecycle.
 * @author Despical
 * <p>
 * Created at 18.06.2026
 */
public final class GameStateChangeEvent extends GameEvent implements Cancellable {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private boolean cancelled;
    private final GameState oldState;
    private final GameState newState;

    /**
     * Creates a state change event.
     *
     * @param game game whose state is changing
     * @param oldState currently assigned state
     * @param newState requested destination state
     */
    public GameStateChangeEvent(Game game, GameState oldState, GameState newState) {
        super(game);
        this.oldState = oldState;
        this.newState = newState;
    }

    /**
     * Returns the state assigned when the event was fired.
     *
     * @return current state before the transition
     */
    public GameState getOldState() {
        return oldState;
    }

    /**
     * Returns the requested destination state.
     *
     * @return state that will be assigned unless cancelled
     */
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

    /** {@inheritDoc} */
    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
