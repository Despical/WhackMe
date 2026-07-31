package dev.despical.whackme.api;

import dev.despical.whackme.api.event.game.GameEndEvent;
import dev.despical.whackme.api.event.game.GameStartEvent;
import dev.despical.whackme.api.event.game.GameStateChangeEvent;
import dev.despical.whackme.api.event.game.GameStopEvent;
import dev.despical.whackme.api.event.player.PlayerJoinAttemptEvent;
import dev.despical.whackme.api.event.player.PlayerLeaveGameEvent;
import dev.despical.whackme.api.event.player.PlayerStatisticChangeEvent;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

/**
 * Enumerates every custom Bukkit event dispatched through the Whack Me event
 * API.
 * <p>
 * The enum is used by {@link EventManager} to validate event factories and by
 * {@link EventRegistry} for discovery. Bukkit listeners should still register
 * against the concrete event classes.
 *
 * @author Despical
 * <p>
 * Created at 29.01.2026
 */
public enum EventType {

    /**
     * Fired after a game enters active play.
     */
    GAME_START(GameStartEvent.class),

    /**
     * Fired when a game reaches its normal ending phase.
     */
    GAME_END(GameEndEvent.class),

    /**
     * Fired before a game changes state.
     */
    GAME_STATE_CHANGE(GameStateChangeEvent.class),

    /**
     * Fired after a game is forcefully stopped and cleaned up.
     */
    GAME_STOP(GameStopEvent.class),

    /**
     * Fired before a player joins a game.
     */
    PLAYER_JOIN_ATTEMPT(PlayerJoinAttemptEvent.class),

    /**
     * Fired before a player leaves a game.
     */
    PLAYER_LEAVE(PlayerLeaveGameEvent.class),

    /**
     * Fired before a player statistic is stored.
     */
    PLAYER_STAT_CHANGE(PlayerStatisticChangeEvent.class);

    private final Class<? extends Event> eventClass;

    EventType(Class<? extends Event> eventClass) {
        this.eventClass = eventClass;
    }

    /**
     * Returns the concrete Bukkit event class represented by this type.
     *
     * @return the registered event class
     */
    @NotNull
    public Class<? extends Event> getEventClass() {
        return eventClass;
    }
}
