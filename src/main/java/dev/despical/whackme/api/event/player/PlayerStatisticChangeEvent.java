package dev.despical.whackme.api.event.player;

import dev.despical.whackme.stats.StatisticType;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called before a player's statistic value is stored.
 * <p>
 * The event exposes the statistic key, the previous value, and a mutable new
 * value. Listeners may cancel the update or replace the new value before it is
 * written to the player's statistic map. The event covers both persistent and
 * temporary per-run statistics.
 * <p>
 * Common use cases:
 * <ul>
 *   <li>Applying boosters or multipliers</li>
 *   <li>Clamping values to a maximum or minimum</li>
 *   <li>Rejecting invalid statistic changes</li>
 *   <li>Mirroring statistic updates to an external service</li>
 * </ul>
 *
 * @param <T> the value type used by the statistic
 * @author Despical
 * <p>
 * Created at 29.01.2026
 */
public class PlayerStatisticChangeEvent<T> extends PlayerEvent implements Cancellable {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    /**
     * The statistic key being updated.
     */
    @NotNull
    private final StatisticType<T> statisticType;

    /**
     * The value stored before this update was requested.
     */
    private final T oldValue;

    /**
     * The value that will be stored if the event is not cancelled.
     */
    private T newValue;

    /**
     * Whether this statistic update has been cancelled.
     */
    private boolean cancelled;

    /**
     * Constructs a new player statistic change event.
     *
     * @param player the player whose statistic is changing
     * @param statisticType the statistic key being updated
     * @param oldValue the value stored before the update
     * @param newValue the value that will be stored unless modified or cancelled
     */
    public PlayerStatisticChangeEvent(@NotNull Player player, @NotNull StatisticType<T> statisticType,
                                      T oldValue, T newValue) {
        super(player);
        this.statisticType = statisticType;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    /**
     * Returns the statistic definition being updated.
     *
     * @return the statistic key
     */
    @NotNull
    public StatisticType<T> getStatisticType() {
        return statisticType;
    }

    /**
     * Returns the value stored before this update.
     *
     * @return previous statistic value
     */
    public T getOldValue() {
        return oldValue;
    }

    /**
     * Returns the value that will be stored if the event is not cancelled.
     *
     * @return proposed statistic value
     */
    public T getNewValue() {
        return newValue;
    }

    /**
     * Replaces the value that will be stored.
     *
     * @param newValue replacement statistic value
     */
    public void setNewValue(T newValue) {
        this.newValue = newValue;
    }

    /**
     * Returns whether the statistic update has been cancelled.
     *
     * @return {@code true} when the value must not be stored
     */
    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * Sets whether the statistic update should be discarded.
     *
     * @param cancel {@code true} to reject the update
     */
    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
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
     * Returns a compact debug representation of the statistic transition.
     *
     * @return a string containing the player, statistic, values, and cancellation state
     */
    @Override
    public String toString() {
        return "player=%s, stat=%s, oldValue=%s, newValue=%s, cancelled=%s".formatted(
            getPlayer().getName(),
            statisticType.getKey(),
            String.valueOf(oldValue),
            String.valueOf(newValue),
            cancelled
        );
    }
}
