package dev.despical.whackme.api.event;

import org.bukkit.event.Event;

/**
 * Base type for every Bukkit event emitted by Whack Me.
 * <p>
 * Each concrete event owns an independent Bukkit {@code HandlerList}. This
 * class is only a shared marker for API discovery and should not be registered
 * as a listener target directly.
 *
 * @author Despical
 * <p>
 * Created at 29.01.2026
 */
public abstract class WhackMeEvent extends Event {
}
