package dev.despical.whackme.api.event.player;

import dev.despical.whackme.stats.StatisticType;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called before a player's statistic value is stored in the Whack Me user
 * model.
 * <p>
 * Listeners may cancel the update or replace {@link #getNewValue()} through
 * {@link #setNewValue(Object)}. The event covers both persistent statistics and
 * temporary per-run statistics.
 *
 * <pre>{@code
 * @EventHandler
 * public void onStatisticChange(PlayerStatisticChangeEvent<Integer> event) {
 *     if (event.getStatisticType() == Statistics.LOCAL_SCORE) {
 *         event.setNewValue(Math.max(0, event.getNewValue()));
 *     }
 * }
 * }</pre>
 *
 * @param <T> value type of the statistic
 * @apiNote Database loading bypasses this event; it represents runtime changes
 * requested through the user statistics API.
 * @author Despical
 * <p>
 * Created at 29.01.2026
 */
public class PlayerStatisticChangeEvent<T> extends PlayerEvent implements Cancellable {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final StatisticType<T> statisticType;
    private final T oldValue;
    private T newValue;
    private boolean cancelled;

    /**
     * Creates a statistic change event.
     *
     * @param player player whose statistic is changing
     * @param statisticType statistic definition being updated
     * @param oldValue value currently stored
     * @param newValue requested replacement value
     */
    public PlayerStatisticChangeEvent(Player player, StatisticType<T> statisticType, T oldValue, T newValue) {
        super(player);
        this.statisticType = statisticType;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    /**
     * Returns the statistic definition being updated.
     *
     * @return statistic definition
     */
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
