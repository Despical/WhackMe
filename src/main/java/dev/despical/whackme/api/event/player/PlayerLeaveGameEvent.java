package dev.despical.whackme.api.event.player;

import dev.despical.whackme.arena.Arena;
import dev.despical.whackme.game.Game;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called immediately before a player is removed from a Whack Me game.
 * <p>
 * At dispatch time the player and their temporary run statistics are still
 * attached to the {@link Game}; cleanup, teleportation, and temporary-stat
 * reset have not run yet. The event is therefore suitable for analytics,
 * custom announcements, or capturing final run data.
 * <p>
 * This event is informational and is not cancellable. The {@link LeaveReason}
 * identifies whether the removal came from normal completion, a player
 * command, disconnection, administration, or game shutdown.
 *
 * @author Despical
 * <p>
 * Created at 18.06.2026
 */
public final class PlayerLeaveGameEvent extends PlayerEvent {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    /**
     * The game the player is leaving.
     */
    @NotNull
    private final Game game;

    /**
     * The reason the player is being removed from the game.
     */
    @NotNull
    private final LeaveReason reason;

    /**
     * Constructs a new player leave event.
     *
     * @param player the player leaving the game
     * @param game the game the player is leaving
     * @param reason the reason that initiated the departure
     */
    public PlayerLeaveGameEvent(@NotNull Player player, @NotNull Game game, @NotNull LeaveReason reason) {
        super(player);
        this.game = game;
        this.reason = reason;
    }

    /**
     * Returns the game the player is leaving.
     *
     * @return the current game
     */
    @NotNull
    public Game getGame() {
        return game;
    }

    /**
     * Returns the cause of the departure.
     *
     * @return the leave reason
     */
    @NotNull
    public LeaveReason getReason() {
        return reason;
    }

    /**
     * Returns the arena the player is leaving.
     *
     * @return the current arena
     */
    @NotNull
    public Arena getArena() {
        return game.getArena();
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
     * Returns a compact debug representation of the game departure.
     *
     * @return a string containing the player, arena, and leave reason
     */
    @Override
    public String toString() {
        return "player=%s, arena=%s, reason=%s"
            .formatted(getPlayer().getName(), getArena().getId(), reason);
    }

    /**
     * Describes why a player is being removed from a Whack Me game.
     */
    public enum LeaveReason {

        /**
         * The normal game timer expired and the run finished.
         */
        FINISH,

        /**
         * The player used the leave command.
         */
        LEAVE_COMMAND,

        /**
         * An administrator kicked the player from the game.
         */
        KICK,

        /**
         * The player disconnected from the server.
         */
        QUIT,

        /**
         * A plugin or server reload interrupted the game.
         */
        RELOAD,

        /**
         * Server shutdown interrupted the game.
         */
        SHUTDOWN,

        /**
         * An administrator force-stopped the game.
         */
        STOP_COMMAND,

        /**
         * The active arena was deleted.
         */
        ARENA_DELETED
    }
}
