/**
 * Provides Whack Me events associated with individual players.
 * <p>
 * Join attempts are cancellable before game membership changes occur. Player
 * departures are informational and expose a leave reason, while statistic
 * changes allow listeners to cancel or replace the value that will be stored.
 */
package dev.despical.whackme.api.event.player;
