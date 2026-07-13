package dev.despical.whackme.game.states;

import dev.despical.whackme.game.Game;
import dev.despical.whackme.game.GameState;
import dev.despical.whackme.option.IntOption;
import dev.despical.whackme.user.User;

/**
 * @author Despical
 * <p>
 * Created at 13.07.2026
 */
public final class InGameState extends GameStateHandler {

    public InGameState(Game game) {
        super(game);
    }

    @Override
    public void firstTick() {
        game.setTimer(IntOption.GAMEPLAY_TIME.value());
        game.getBossBarManager().setProgress(1F);
        game.getPointHandler().start();

        plugin.getEventManager().gameStart(game);
    }

    @Override
    public void tick() {
        int timer = game.getTimer();

        if (timer <= 0) {
            game.setState(GameState.ENDING);
            return;
        }

        game.setTimer(timer - 1);
        game.getBossBarManager().setProgress((float) game.getTimer() / Math.max(1, IntOption.GAMEPLAY_TIME.value()));
    }

    @Override
    public void join(User user) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void leave(User user) {
        plugin.getGameManager().finishGame(game, true, true, true);
        game.setState(GameState.RESTARTING);
    }
}
