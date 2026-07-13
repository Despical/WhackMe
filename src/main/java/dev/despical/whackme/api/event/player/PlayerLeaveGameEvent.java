package dev.despical.whackme.api.event.player;

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
 *
 * <pre>{@code
 * @EventHandler
 * public void onLeave(PlayerLeaveGameEvent event) {
 *     plugin.getLogger().info(event.getPlayer().getName()
 *         + " left because of " + event.getReason());
 * }
 * }</pre>
 *
 * @apiNote This event is informational and cannot prevent the player from
 * leaving the game.
 * @author Despical
 * <p>
 * Created at 18.06.2026
 */
public final class PlayerLeaveGameEvent extends PlayerEvent {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final Game game;
    private final LeaveReason reason;

    /**
     * Creates a player leave event.
     *
     * @param player player leaving the game
     * @param game game the player is leaving
     * @param reason reason that initiated the departure
     */
    public PlayerLeaveGameEvent(Player player, Game game, LeaveReason reason) {
        super(player);
        this.game = game;
        this.reason = reason;
    }

    /**
     * Returns the game the player is leaving.
     *
     * @return related game
     */
    public Game getGame() {
        return game;
    }

    /**
     * Returns the cause of the departure.
     *
     * @return leave reason
     */
    public LeaveReason getReason() {
        return reason;
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

    /**
     * Describes why a player is being removed from a game.
     *
     * @author Despical
     * <p>
     * Created at 18.06.2026
     */
    public enum LeaveReason {

        /** The normal game timer expired and the run finished. */
        FINISH,

        /** The player used the leave command. */
        LEAVE_COMMAND,

        /** An administrator kicked the player from the game. */
        KICK,

        /** The player disconnected from the server. */
        QUIT,

        /** A plugin or server reload interrupted the game. */
        RELOAD,

        /** Server shutdown interrupted the game. */
        SHUTDOWN,

        /** An administrator force-stopped the game. */
        STOP_COMMAND,

        /** The active arena was deleted. */
        ARENA_DELETED
    }
}
