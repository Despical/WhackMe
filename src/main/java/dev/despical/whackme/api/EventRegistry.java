package dev.despical.whackme.api;

import org.bukkit.event.Event;

import java.util.EnumSet;
import java.util.Set;

/**
 * Provides read-only discovery helpers for the events exposed by Whack Me.
 * <p>
 * This class deliberately contains no mutable registration state: the event
 * catalog is defined by {@link EventType}, while actual listener registration
 * continues to use Bukkit's standard plugin manager.
 * <p>
 * API Note: Use this registry when building generic integrations such as event
 * browsers or profilers. Normal listeners should register against the concrete
 * event classes.
 * @author Despical
 * <p>
 * Created at 29.01.2026
 */
public final class EventRegistry {

    /**
     * Prevents utility class instantiation.
     */
    private EventRegistry() {
    }

    /**
     * Resolves the concrete Bukkit event class represented by a logical type.
     *
     * @param type event type to resolve
     * @return concrete Bukkit event class associated with {@code type}
     */
    public static Class<? extends Event> getEventClass(EventType type) {
        return type.getEventClass();
    }

    /**
     * Tests whether an event instance belongs to the supplied logical type.
     * Subclasses are accepted through {@link Class#isInstance(Object)}.
     *
     * @param type expected logical event type
     * @param event event instance to test
     * @return {@code true} when the event is compatible with the mapped class
     */
    public static boolean matches(EventType type, Event event) {
        return type.getEventClass().isInstance(event);
    }

    /**
     * Returns a fresh set containing every event type exposed by the API.
     *
     * @return all currently registered logical event types
     */
    public static Set<EventType> getRegisteredTypes() {
        return EnumSet.allOf(EventType.class);
    }
}
