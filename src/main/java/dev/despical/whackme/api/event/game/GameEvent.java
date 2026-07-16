package dev.despical.whackme.api.event.game;

import dev.despical.whackme.api.event.WhackMeEvent;
import dev.despical.whackme.arena.Arena;
import dev.despical.whackme.game.Game;
import org.jetbrains.annotations.NotNull;

/**
 * Base class for events associated with one Whack Me game session.
 * <p>
 * The related {@link Game} provides access to the player, point handler,
 * scoreboard, boss bar, timer, state, and arena configuration. The exact
 * lifecycle guarantees depend on the concrete event.
API note: A {@code Game} is a live runtime object. Do not mutate it from
 * asynchronous event handlers.
 * @author Despical
 * <p>
 * Created at 18.06.2026
 */
public abstract class GameEvent extends WhackMeEvent {

    protected final Game game;

    /**
     * Creates a game-scoped Whack Me event.
     *
     * @param game game associated with the event
     */
    protected GameEvent(Game game) {
        this.game = game;
    }

    /**
     * Returns the live game associated with this event.
     *
     * @return related game
     */
    public final Game getGame() {
        return game;
    }

    /**
     * Returns the arena that owns the related game.
     *
     * @return game arena
     */
    @NotNull
    public Arena getArena() {
        return game.getArena();
    }

    /**
     * Returns a compact representation suitable for profiler output.
     *
     * @return arena detail string
     */
    @Override
    public String toString() {
        return "[arena=%s]".formatted(game.getArena());
    }
}
