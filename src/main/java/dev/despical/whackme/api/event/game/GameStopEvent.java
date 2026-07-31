package dev.despical.whackme.api.event.game;

import dev.despical.whackme.game.Game;
import dev.despical.whackme.game.StopReason;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

/**
 * Called after a Whack Me game has been forcefully stopped and cleaned up.
 * <p>
 * The active user has already been detached from the {@link Game}. Use
 * {@link #getStoppedPlayers()} to identify the affected player and
 * {@link #getStopReason()} to distinguish administrative stops, reloads,
 * shutdowns, and arena deletion.
 * <p>
 * The stopped-player list is an immutable snapshot captured before
 * cleanup. A stop event is not a normal completion and does not apply run
 * statistics.
 * <p>
 * This event is informational and is not cancellable.
 *
 * @author Despical
 * <p>
 * Created at 18.06.2026
 */
public final class GameStopEvent extends GameEvent {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    /**
     * The reason why the game was stopped.
     */
    @NotNull
    private final StopReason stopReason;

    /**
     * Immutable snapshot of player UUIDs present before cleanup.
     */
    @NotNull
    private final List<UUID> stoppedPlayers;

    /**
     * Constructs a new game stop event.
     *
     * @param game the game that was stopped
     * @param stopReason the reason that triggered the stop
     * @param stoppedPlayers the players attached before cleanup
     */
    public GameStopEvent(@NotNull Game game, @NotNull StopReason stopReason,
                         @NotNull List<UUID> stoppedPlayers) {
        super(game);
        this.stopReason = stopReason;
        this.stoppedPlayers = List.copyOf(stoppedPlayers);
    }

    /**
     * Returns the reason the game was stopped.
     *
     * @return the stop reason
     */
    @NotNull
    public StopReason getStopReason() {
        return stopReason;
    }

    /**
     * Returns an immutable snapshot of players affected by the stop.
     *
     * @return the stopped player UUIDs
     */
    @NotNull
    public List<UUID> getStoppedPlayers() {
        return stoppedPlayers;
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
     * Returns a compact debug representation of the stopped game.
     *
     * @return a string containing the arena, reason, and player snapshot
     */
    @Override
    public String toString() {
        return "arena=%s, reason=%s, stoppedPlayers=%s"
            .formatted(getArena().getId(), stopReason, stoppedPlayers);
    }
}
