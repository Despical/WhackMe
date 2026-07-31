package dev.despical.whackme.api.event.game;

import dev.despical.whackme.game.Game;
import dev.despical.whackme.stats.Statistics;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a Whack Me run reaches its normal ending phase.
 * <p>
 * Point generation has stopped, but result persistence and player cleanup have
 * not run yet. The game still contains its user, so temporary run statistics
 * and {@link #getFinalScore()} are available to listeners.
 * <p>
 * This event is fired before personal and arena records are updated and
 * before temporary statistics are reset. It is not fired for force-stopped
 * games; use {@link GameStopEvent} for that lifecycle.
 * <p>
 * This event is informational and is not cancellable.
 *
 * @author Despical
 * <p>
 * Created at 29.01.2026
 */
public final class GameEndEvent extends GameEvent {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    /**
     * Constructs a new normal game end event.
     *
     * @param game the game that reached its ending phase
     */
    public GameEndEvent(@NotNull Game game) {
        super(game);
    }

    /**
     * Returns the score accumulated during the completed run.
     *
     * @return final local score, or {@code 0} if the game no longer has a user
     */
    public int getFinalScore() {
        return game.getUser() == null ? 0 : game.getUser().getStatistic(Statistics.LOCAL_SCORE);
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
}
