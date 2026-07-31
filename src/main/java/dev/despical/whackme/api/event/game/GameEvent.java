package dev.despical.whackme.api.event.game;

import dev.despical.whackme.api.event.WhackMeEvent;
import dev.despical.whackme.arena.Arena;
import dev.despical.whackme.game.Game;
import org.jetbrains.annotations.NotNull;

/**
 * Base class for events associated with one Whack Me game instance.
 * <p>
 * The related {@link Game} provides access to the active player, point
 * handler, scoreboard, boss bar, timer, state, and {@link Arena} configuration.
 * The exact lifecycle guarantees depend on the concrete event.
 *
 * @author Despical
 * <p>
 * Created at 18.06.2026
 */
public abstract class GameEvent extends WhackMeEvent {

    /**
     * The game instance associated with this event.
     */
    @NotNull
    protected final Game game;

    /**
     * Constructs a new game event.
     *
     * @param game the game associated with the event
     */
    protected GameEvent(@NotNull Game game) {
        this.game = game;
    }

    /**
     * Returns the game instance associated with this event.
     *
     * @return the related game
     */
    @NotNull
    public final Game getGame() {
        return game;
    }

    /**
     * Returns the arena represented by the associated game.
     *
     * @return the related arena
     */
    @NotNull
    public Arena getArena() {
        return game.getArena();
    }

    /**
     * Returns a compact debug representation containing the arena identifier.
     *
     * @return a string containing the event arena
     */
    @Override
    public String toString() {
        return "arena=%s".formatted(getArena().getId());
    }
}
