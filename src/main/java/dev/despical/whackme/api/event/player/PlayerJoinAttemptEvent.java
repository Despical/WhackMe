package dev.despical.whackme.api.event.player;

import dev.despical.whackme.game.Game;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called after built-in arena checks pass but before a player is added to a
 * Whack Me game.
 * <p>
 * Cancelling the event prevents the join without modifying the target
 * {@link Game}. This is the extension point for custom permissions, queues,
 * maintenance rules, or third-party cooldowns.
 *
 * <pre>{@code
 * @EventHandler
 * public void onJoinAttempt(PlayerJoinAttemptEvent event) {
 *     if (maintenanceArenas.contains(event.getGame().getArena().getId())) {
 *         event.setCancelled(true);
 *         event.getPlayer().sendMessage("This arena is under maintenance.");
 *     }
 * }
 * }</pre>
 *
 * @apiNote Cancellation does not send a message automatically. A listener
 * cancelling the attempt should explain the reason to the player when useful.
 * @author Despical
 * <p>
 * Created at 18.06.2026
 */
public final class PlayerJoinAttemptEvent extends PlayerEvent implements Cancellable {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final Game game;
    private boolean cancelled;

    /**
     * Creates a join attempt event.
     *
     * @param player player attempting to join
     * @param game target game
     */
    public PlayerJoinAttemptEvent(Player player, Game game) {
        super(player);
        this.game = game;
    }

    /**
     * Returns the game the player is attempting to join.
     *
     * @return target game
     */
    public Game getGame() {
        return game;
    }

    /**
     * Returns whether a listener has denied this join attempt.
     *
     * @return {@code true} when the join is cancelled
     */
    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * Sets whether the player should be prevented from joining.
     *
     * @param cancel {@code true} to deny the join attempt
     */
    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
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
}
