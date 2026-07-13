package dev.despical.whackme.api.event.game;

import dev.despical.whackme.game.Game;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called after a Whack Me game enters active play.
 * <p>
 * The gameplay timer has been initialized, the boss bar is active, and the
 * point-block handler has started before this event is dispatched. The player
 * is already attached to the {@link Game}.
 *
 * <pre>{@code
 * @EventHandler
 * public void onGameStart(GameStartEvent event) {
 *     event.getGame().getPlayer().sendMessage("Good luck!");
 * }
 * }</pre>
 *
 * @apiNote This event is informational and is not cancellable. To deny a
 * player entry, listen to {@code PlayerJoinAttemptEvent} instead.
 * @author Despical
 * <p>
 * Created at 18.06.2026
 */
public final class GameStartEvent extends GameEvent {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    /**
     * Creates a game start event.
     *
     * @param game game that entered active play
     */
    public GameStartEvent(Game game) {
        super(game);
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
