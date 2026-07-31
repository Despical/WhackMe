package dev.despical.whackme.api.event.game;

import dev.despical.whackme.game.Game;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called after a Whack Me game has entered active play.
 * <p>
 * The gameplay timer has been initialized, the boss bar is active, and the
 * point-block handler has started before this event is dispatched. The player
 * is already attached to the {@link Game}.
 * <p>
 * This event is informational and is not cancellable. To deny player entry,
 * listen to {@code PlayerJoinAttemptEvent} instead.
 *
 * @author Despical
 * <p>
 * Created at 18.06.2026
 */
public final class GameStartEvent extends GameEvent {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    /**
     * Constructs a new game start event.
     *
     * @param game the game that entered active play
     */
    public GameStartEvent(@NotNull Game game) {
        super(game);
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
