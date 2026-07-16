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
 *
 * <pre>{@code
 * @EventHandler
 * public void onGameStop(GameStopEvent event) {
 *     audit.log(event.getStopReason(), event.getStoppedPlayers());
 * }
 * }</pre>
API note: The stopped-player list is an immutable snapshot captured before
 * cleanup. A stop event is not a normal completion and does not apply run
 * statistics.
 * @author Despical
 * <p>
 * Created at 18.06.2026
 */
public final class GameStopEvent extends GameEvent {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final StopReason stopReason;
    private final List<UUID> stoppedPlayers;

    /**
     * Creates a game stop event.
     *
     * @param game game that was stopped
     * @param stopReason reason that triggered the stop
     * @param stoppedPlayers players attached before cleanup
     */
    public GameStopEvent(Game game, StopReason stopReason, List<UUID> stoppedPlayers) {
        super(game);
        this.stopReason = stopReason;
        this.stoppedPlayers = List.copyOf(stoppedPlayers);
    }

    /**
     * Returns the reason the game was stopped.
     *
     * @return stop reason
     */
    public StopReason getStopReason() {
        return stopReason;
    }

    /**
     * Returns an immutable snapshot of players affected by the stop.
     *
     * @return stopped player UUIDs
     */
    public List<UUID> getStoppedPlayers() {
        return stoppedPlayers;
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
