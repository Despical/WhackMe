package dev.despical.whackme.game.states;

import dev.despical.whackme.game.Game;
import dev.despical.whackme.game.GameState;
import dev.despical.whackme.user.User;

/**
 * @author Despical
 * <p>
 * Created at 13.07.2026
 */
public final class RestartingState extends GameStateHandler {

    public RestartingState(Game game) {
        super(game);
    }

    @Override
    public void firstTick() {
        game.getPointHandler().clear();
        game.clearUser();
        game.setTimer(0);
        game.setState(GameState.WAITING);
    }

    @Override
    public void tick() {
        game.setState(GameState.WAITING);
    }

    @Override
    public void join(User user) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void leave(User user) {
    }
}
