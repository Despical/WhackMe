/**
 * Provides lifecycle events associated with complete Whack Me game sessions.
 * <p>
 * Game events expose both the live game object and its arena. Start, end, and
 * state-change events are emitted before their related cleanup completes,
 * while stop events include immutable player snapshots captured before the
 * active user is detached.
 */
package dev.despical.whackme.api.event.game;
