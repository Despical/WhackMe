/**
 * Provides Whack Me custom event discovery, dispatch, and diagnostics.
 * <p>
 * External plugins normally consume this API by registering Bukkit listeners
 * for classes under {@code dev.despical.whackme.api.event}. {@link
 * dev.despical.whackme.api.EventType} and {@link
 * dev.despical.whackme.api.EventRegistry} support generic discovery, while the
 * runtime-owned {@link dev.despical.whackme.api.EventManager} dispatches events.
 */
package dev.despical.whackme.api;
