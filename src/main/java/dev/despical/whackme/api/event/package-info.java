/**
 * Defines the common base type for Bukkit events emitted by Whack Me.
 * <p>
 * Every concrete event extends {@link
 * dev.despical.whackme.api.event.WhackMeEvent} and owns a separate Bukkit
 * handler list. Consumers should listen to concrete player or game event
 * classes rather than the marker base class.
 */
package dev.despical.whackme.api.event;
