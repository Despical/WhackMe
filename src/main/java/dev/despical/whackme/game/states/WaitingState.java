package dev.despical.whackme.game.states;

import dev.despical.whackme.arena.options.ArenaKeys;
import dev.despical.whackme.game.Game;
import dev.despical.whackme.user.User;

/**
 * @author Despical
 * <p>
 * Created at 13.07.2026
 */
public final class WaitingState extends GameStateHandler {

    public WaitingState(Game game) {
        super(game);
    }

    @Override
    public void tick() {
    }

    @Override
    public void join(User user) {
        plugin.getGameManager().preparePlayer(game, user);

        user.ifPlayerPresent(player -> player.teleport(arena.getOption(ArenaKeys.START_LOCATION)));
    }

    @Override
    public void leave(User user) {
    }
}
