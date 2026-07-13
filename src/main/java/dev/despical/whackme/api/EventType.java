package dev.despical.whackme.api;

import dev.despical.whackme.api.event.game.GameEndEvent;
import dev.despical.whackme.api.event.game.GameStartEvent;
import dev.despical.whackme.api.event.game.GameStateChangeEvent;
import dev.despical.whackme.api.event.game.GameStopEvent;
import dev.despical.whackme.api.event.player.PlayerJoinAttemptEvent;
import dev.despical.whackme.api.event.player.PlayerLeaveGameEvent;
import dev.despical.whackme.api.event.player.PlayerStatisticChangeEvent;
import org.bukkit.event.Event;

/**
 * Lists every Bukkit event exposed by the Whack Me API and associates each
 * logical event type with its concrete implementation class.
 * <p>
 * The mapping is used by {@link EventManager#callByType(EventType, java.util.function.Supplier)}
 * to detect accidental mismatches between an event identifier and the event
 * supplied by internal dispatch code.
 *
 * @apiNote Plugin integrations normally listen to the concrete Bukkit event
 * class directly. This enum is primarily useful for discovery, diagnostics,
 * and generic event tooling.
 * @author Despical
 * <p>
 * Created at 29.01.2026
 */
public enum EventType {

    /** A game has entered active play. */
    GAME_START(GameStartEvent.class),

    /** A game has reached its normal ending phase. */
    GAME_END(GameEndEvent.class),

    /** A game is about to transition to another state. */
    GAME_STATE_CHANGE(GameStateChangeEvent.class),

    /** A game has been forcefully stopped. */
    GAME_STOP(GameStopEvent.class),

    /** A player is attempting to join a game. */
    PLAYER_JOIN_ATTEMPT(PlayerJoinAttemptEvent.class),

    /** A player is about to leave a game. */
    PLAYER_LEAVE(PlayerLeaveGameEvent.class),

    /** A player's stored or temporary statistic is about to change. */
    PLAYER_STAT_CHANGE(PlayerStatisticChangeEvent.class);

    private final Class<? extends Event> eventClass;

    /**
     * Creates an event type mapping.
     *
     * @param eventClass concrete Bukkit event class represented by the type
     */
    EventType(Class<? extends Event> eventClass) {
        this.eventClass = eventClass;
    }

    /**
     * Returns the concrete Bukkit event class represented by this type.
     *
     * @return concrete event implementation class
     */
    public Class<? extends Event> getEventClass() {
        return eventClass;
    }
}
