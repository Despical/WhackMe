package dev.despical.whackme.game.states;

import dev.despical.whackme.game.Game;
import dev.despical.whackme.user.User;

/**
 * @author Despical
 * <p>
 * Created at 13.07.2026
 */
public non-sealed abstract class GameStateHandler extends GameStateBase {

    protected GameStateHandler(Game game) {
        super(game);
    }

    public abstract void tick();

    public abstract void join(User user);

    public abstract void leave(User user);

    public void firstTick() {
    }
}
