package dev.despical.whackme.game.states;

import dev.despical.whackme.WhackMe;
import dev.despical.whackme.api.EventManager;
import dev.despical.whackme.arena.Arena;
import dev.despical.whackme.chat.ChatManager;
import dev.despical.whackme.game.Game;
import dev.despical.whackme.game.GameManager;

/**
 * @author Despical
 * <p>
 * Created at 13.07.2026
 */
abstract sealed class GameStateBase permits GameStateHandler {

    protected static final WhackMe plugin = WhackMe.getInstance();

    protected final Game game;
    protected final Arena arena;

    protected GameStateBase(Game game) {
        this.game = game;
        this.arena = game.getArena();
    }
}
