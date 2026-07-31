package dev.despical.whackme.api;

import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * Provides read-only discovery and type checks for Whack Me custom events.
 * <p>
 * The registry is backed by {@link EventType}; no runtime registration is
 * required. It is primarily useful for diagnostics, integrations, and generic
 * event tooling.
 *
 * @author Despical
 * <p>
 * Created at 29.01.2026
 */
public final class EventRegistry {

    private static final Set<EventType> REGISTERED_TYPES =
        Collections.unmodifiableSet(EnumSet.allOf(EventType.class));

    private EventRegistry() {
    }

    /**
     * Resolves the concrete Bukkit event class for an event type.
     *
     * @param type the event type to resolve
     * @return the concrete event class
     */
    @NotNull
    public static Class<? extends Event> getEventClass(@NotNull EventType type) {
        return type.getEventClass();
    }

    /**
     * Tests whether an event instance matches a registered event type.
     *
     * @param type the expected event type
     * @param event the event instance to inspect
     * @return {@code true} when the event is an instance of the registered class
     */
    public static boolean matches(@NotNull EventType type, @NotNull Event event) {
        return type.getEventClass().isInstance(event);
    }

    /**
     * Returns every event type known to this plugin version.
     *
     * @return an unmodifiable set in enum declaration order
     */
    @NotNull
    public static Set<EventType> getRegisteredTypes() {
        return REGISTERED_TYPES;
    }
}
