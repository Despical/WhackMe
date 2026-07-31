package dev.despical.whackme.api.event.player;

import dev.despical.whackme.WhackMe;
import dev.despical.whackme.api.event.WhackMeEvent;
import dev.despical.whackme.user.User;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Base class for Whack Me events associated with one Bukkit player.
 * <p>
 * Use {@link #getPlayer()} for Bukkit operations and {@link #getUser()} for
 * Whack Me-specific state such as statistics and current game membership.
 *
 * @author Despical
 * <p>
 * Created at 29.01.2026
 */
public abstract class PlayerEvent extends WhackMeEvent {

    /**
     * The Bukkit player associated with this event.
     */
    @NotNull
    private final Player player;

    /**
     * Constructs a new player event.
     *
     * @param player the Bukkit player associated with the event
     */
    protected PlayerEvent(@NotNull Player player) {
        this.player = player;
    }

    /**
     * Returns the Bukkit player associated with this event.
     *
     * @return the event player
     */
    @NotNull
    public final Player getPlayer() {
        return player;
    }

    /**
     * Returns the plugin-specific {@link User} for this player.
     *
     * @return the Whack Me user representing the player
     */
    @NotNull
    public final User getUser() {
        return WhackMe.getInstance().getUserManager().getUser(player);
    }

    /**
     * Returns a compact debug representation containing the player name.
     *
     * @return a string containing the event player
     */
    @Override
    public String toString() {
        return "player=%s".formatted(player.getName());
    }
}
