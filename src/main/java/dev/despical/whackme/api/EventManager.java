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

import java.util.function.Supplier;
import java.util.List;
import java.util.UUID;

/**
 * Central dispatcher for every Bukkit event emitted by Whack Me.
 * <p>
 * Besides invoking Bukkit's plugin manager, the dispatcher records event
 * timings when profiling is enabled and offers typed convenience methods for
 * the plugin's game lifecycle.
 *
 * <p>External plugins usually consume this API through Bukkit listeners:</p>
 * <pre>{@code
 * @EventHandler
 * public void onJoinAttempt(PlayerJoinAttemptEvent event) {
 *     if (!event.getPlayer().hasPermission("whackme.play")) {
 *         event.setCancelled(true);
 *     }
 * }
 * }</pre>
 * <p>
 * API Note: Event dispatch must occur on a thread accepted by the relevant
 * Bukkit event. Whack Me lifecycle helpers are intended to be called from the
 * server thread.
 * @author Despical
 * <p>
 * Created at 29.01.2026
 */
public final class EventManager {

    private final EventProfiler profiler;

    /**
     * Creates the dispatcher and its profiler using the current configuration.
     *
     * @param plugin active Whack Me plugin instance
     */
    public EventManager(WhackMe plugin) {
        this.profiler = new EventProfiler(plugin);
    }

    /**
     * Dispatches an event through Bukkit and records its listener execution
     * duration when profiling is enabled.
     *
     * @param event event instance to dispatch
     * @param <T> concrete event type
     * @return the same event instance after listeners have processed it
     */
    public <T extends Event> T call(T event) {
        long start = System.nanoTime();
        Bukkit.getPluginManager().callEvent(event);

        long duration = System.nanoTime() - start;
        profiler.record(event, duration);
        return event;
    }

    /**
     * Creates and dispatches an event while validating it against the expected
     * {@link EventType} mapping.
     *
     * @param type expected logical event type
     * @param supplier supplier that creates the event to dispatch
     * @param <T> concrete event type
     * @return the dispatched event after listener processing
     * @throws IllegalArgumentException if the supplied event does not match the
     * mapped class
     */
    public <T extends Event> T callByType(EventType type, Supplier<T> supplier) {
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
     * Sends the currently collected event timing report to a command sender.
     *
     * @param sender recipient of the formatted report
     */
    public void sendTimingsReport(CommandSender sender) {
        profiler.sendReport(sender);
    }

    /**
     * Reloads event profiling flags from the plugin configuration.
     */
    public void reload() {
        profiler.reload();
    }

    /**
     * Announces that a game has entered active play.
     *
     * @param game game that has started
     */
    public void gameStart(Game game) {
        callByType(EventType.GAME_START, () -> new GameStartEvent(game));
    }

    /**
     * Announces that a game has reached its normal ending phase.
     *
     * @param game game that has ended
     */
    public void gameEnd(Game game) {
        callByType(EventType.GAME_END, () -> new GameEndEvent(game));
    }

    /**
     * Fires the cancellable event raised before a game state transition.
     *
     * @param game game whose state is changing
     * @param oldState state currently assigned to the game
     * @param newState requested destination state
     * @return processed state change event
     */
    public GameStateChangeEvent gameStateChange(Game game, GameState oldState, GameState newState) {
        return callByType(EventType.GAME_STATE_CHANGE, () -> new GameStateChangeEvent(game, oldState, newState));
    }

    /**
     * Announces that a game was forcefully stopped.
     *
     * @param game stopped game
     * @param reason reason that triggered the stop
     * @param stoppedPlayers immutable snapshot source of affected player UUIDs
     */
    public void gameStop(Game game, StopReason reason, List<UUID> stoppedPlayers) {
        callByType(EventType.GAME_STOP, () -> new GameStopEvent(game, reason, stoppedPlayers));
    }

    /**
     * Fires the cancellable event raised before a player is added to a game.
     *
     * @param player player attempting to join
     * @param game target game
     * @return processed join attempt event
     */
    public PlayerJoinAttemptEvent playerJoinAttempt(Player player, Game game) {
        return callByType(EventType.PLAYER_JOIN_ATTEMPT, () -> new PlayerJoinAttemptEvent(player, game));
    }

    /**
     * Announces that a player is about to be removed from a game.
     *
     * @param player player leaving the game
     * @param game game the player is leaving
     * @param reason reason for the departure
     */
    public void playerLeave(Player player, Game game, PlayerLeaveGameEvent.LeaveReason reason) {
        callByType(EventType.PLAYER_LEAVE, () -> new PlayerLeaveGameEvent(player, game, reason));
    }

    /**
     * Fires the cancellable event raised before a statistic value is stored.
     *
     * @param player player whose statistic is changing
     * @param stat statistic definition being updated
     * @param oldValue value currently stored
     * @param newValue requested replacement value
     * @param <T> statistic value type
     * @return processed statistic change event
     */
    public <T> PlayerStatisticChangeEvent<T> statChange(Player player, StatisticType<T> stat, T oldValue, T newValue) {
        return callByType(EventType.PLAYER_STAT_CHANGE, () -> new PlayerStatisticChangeEvent<>(player, stat, oldValue, newValue));
    }
}
