package dev.despical.whackme.api;

import dev.despical.whackme.WhackMe;
import dev.despical.whackme.api.event.game.GameEndEvent;
import dev.despical.whackme.api.event.game.GameStartEvent;
import dev.despical.whackme.api.event.game.GameStateChangeEvent;
import dev.despical.whackme.api.event.game.GameStopEvent;
import dev.despical.whackme.api.event.player.PlayerJoinAttemptEvent;
import dev.despical.whackme.api.event.player.PlayerLeaveGameEvent;
import dev.despical.whackme.api.event.player.PlayerStatisticChangeEvent;
import dev.despical.whackme.game.Game;
import dev.despical.whackme.game.GameState;
import dev.despical.whackme.game.StopReason;
import dev.despical.whackme.stats.StatisticType;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Central dispatcher for Whack Me custom events.
 * <p>
 * The manager invokes Bukkit listeners synchronously, records optional timing
 * information, and validates named event factories against {@link EventType}.
 * External plugins should listen to the concrete event classes rather than
 * manually invoking the lifecycle factory methods in this class.
 *
 * @author Despical
 * <p>
 * Created at 29.01.2026
 */
@ApiStatus.Internal
public final class EventManager {

    private final EventProfiler profiler;

    /**
     * Creates the event dispatcher used by the plugin runtime.
     *
     * @param plugin the owning Whack Me instance
     */
    public EventManager(@NotNull WhackMe plugin) {
        this.profiler = new EventProfiler(plugin);
    }

    /**
     * Dispatches a Bukkit event and records its listener execution time.
     *
     * @param event the event to dispatch
     * @param <T> the concrete event type
     * @return the same event instance after listeners have run
     */
    @NotNull
    public <T extends Event> T call(@NotNull T event) {
        long start = System.nanoTime();
        Bukkit.getPluginManager().callEvent(event);

        long duration = System.nanoTime() - start;
        profiler.record(event, duration);
        return event;
    }

    /**
     * Creates and dispatches an event after validating its registered type.
     *
     * @param type the expected event type
     * @param supplier the factory that creates the event
     * @param <T> the concrete event type
     * @return the event after listeners have run
     * @throws IllegalArgumentException if the factory returns the wrong event class
     */
    @NotNull
    public <T extends Event> T callByType(@NotNull EventType type, @NotNull Supplier<T> supplier) {
        T event = supplier.get();
        Class<? extends Event> expected = EventRegistry.getEventClass(type);

        if (!expected.isInstance(event)) {
            String message = "EventType mismatch! Expected: %s but got: %s"
                .formatted(expected.getSimpleName(), event.getClass().getSimpleName());
            throw new IllegalArgumentException(message);
        }

        return call(event);
    }

    /**
     * Sends the current event timing report to a command sender.
     *
     * @param sender the report recipient
     */
    public void sendTimingsReport(@NotNull CommandSender sender) {
        profiler.sendReport(sender);
    }

    /**
     * Reloads event profiling options from the plugin configuration.
     */
    public void reload() {
        profiler.reload();
    }

    /**
     * Dispatches a game start notification.
     *
     * @param game the game that entered active play
     */
    public void gameStart(@NotNull Game game) {
        callByType(EventType.GAME_START, () -> new GameStartEvent(game));
    }

    /**
     * Dispatches a normal game end notification.
     *
     * @param game the game that reached its ending phase
     */
    public void gameEnd(@NotNull Game game) {
        callByType(EventType.GAME_END, () -> new GameEndEvent(game));
    }

    /**
     * Dispatches a cancellable game state transition.
     *
     * @param game the game whose state is changing
     * @param oldState the state currently assigned to the game
     * @param newState the requested destination state
     * @return the event after listeners have run
     */
    @NotNull
    public GameStateChangeEvent gameStateChange(@NotNull Game game, @NotNull GameState oldState,
                                                @NotNull GameState newState) {
        return callByType(EventType.GAME_STATE_CHANGE, () -> new GameStateChangeEvent(game, oldState, newState));
    }

    /**
     * Dispatches a completed game stop notification.
     *
     * @param game the game that was stopped
     * @param reason the reason for stopping the game
     * @param stoppedPlayers the player UUID snapshot captured before cleanup
     */
    public void gameStop(@NotNull Game game, @NotNull StopReason reason, @NotNull List<UUID> stoppedPlayers) {
        callByType(EventType.GAME_STOP, () -> new GameStopEvent(game, reason, stoppedPlayers));
    }

    /**
     * Dispatches a player game join attempt.
     *
     * @param player the player attempting to join
     * @param game the target game
     * @return the event after listeners have run
     */
    @NotNull
    public PlayerJoinAttemptEvent playerJoinAttempt(@NotNull Player player, @NotNull Game game) {
        return callByType(EventType.PLAYER_JOIN_ATTEMPT, () -> new PlayerJoinAttemptEvent(player, game));
    }

    /**
     * Dispatches a player game departure notification.
     *
     * @param player the player leaving the game
     * @param game the game being left
     * @param reason the reason for the removal
     */
    public void playerLeave(@NotNull Player player, @NotNull Game game,
                            @NotNull PlayerLeaveGameEvent.LeaveReason reason) {
        callByType(EventType.PLAYER_LEAVE, () -> new PlayerLeaveGameEvent(player, game, reason));
    }

    /**
     * Dispatches a mutable player statistic change.
     *
     * @param player the player whose statistic is changing
     * @param stat the statistic key being updated
     * @param oldValue the value stored before the update
     * @param newValue the requested new value
     * @param <T> the statistic value type
     * @return the event after listeners have run
     */
    @NotNull
    public <T> PlayerStatisticChangeEvent<T> statChange(@NotNull Player player, @NotNull StatisticType<T> stat,
                                                        T oldValue, T newValue) {
        return callByType(EventType.PLAYER_STAT_CHANGE, () -> new PlayerStatisticChangeEvent<>(player, stat, oldValue, newValue));
    }
}
