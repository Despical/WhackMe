package dev.despical.whackme.game.states;

import dev.despical.whackme.game.Game;
import dev.despical.whackme.game.GameState;
import dev.despical.whackme.api.event.player.PlayerLeaveGameEvent;
import dev.despical.whackme.rewards.RewardType;
import dev.despical.whackme.user.User;

/**
 * @author Despical
 * <p>
 * Created at 13.07.2026
 */
public final class EndingState extends GameStateHandler {

    public EndingState(Game game) {
        super(game);
    }

    @Override
    public void firstTick() {
        game.getPointHandler().stop();

        var eventManager = plugin.getEventManager();
        if (game.getPlayer() != null) {
            eventManager.playerLeave(game.getPlayer(), game, PlayerLeaveGameEvent.LeaveReason.FINISH);
        }

        eventManager.gameEnd(game);
        plugin.getRewardManager().dispatch(RewardType.GAME_END, game);
        plugin.getGameManager().finishGame(game, true, true, true);

        game.setState(GameState.RESTARTING);
    }

    @Override
    public void tick() {
        game.setState(GameState.RESTARTING);
    }

    @Override
    public void join(User user) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void leave(User user) {
    }
}
