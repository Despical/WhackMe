package dev.despical.whackme.api.event;

import org.bukkit.event.Event;

/**
 * Base type for every Bukkit event emitted by Whack Me.
 * <p>
 * Concrete game and player events extend this class to provide a common API
 * marker. Each concrete Bukkit event still owns its own
 * {@link org.bukkit.event.HandlerList} and must be listened to directly.
 * <p>
 * API Note: Register listeners for a concrete event such as
 * {@code PlayerJoinAttemptEvent}; Bukkit does not dispatch child events to a
 * listener registered only for this base class.
 * @author Despical
 * <p>
 * Created at 29.01.2026
 */
public abstract class WhackMeEvent extends Event {

    /**
     * Creates a base Whack Me event.
     */
    protected WhackMeEvent() {
    }
}
