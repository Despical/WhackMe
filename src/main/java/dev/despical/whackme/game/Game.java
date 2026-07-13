package dev.despical.whackme.game;

import dev.despical.whackme.WhackMe;
import dev.despical.whackme.api.event.game.GameStateChangeEvent;
import dev.despical.whackme.arena.Arena;
import dev.despical.whackme.arena.blocks.PointHandler;
import dev.despical.whackme.bossbar.BossBarManager;
import dev.despical.whackme.game.states.EndingState;
import dev.despical.whackme.game.states.GameStateHandler;
import dev.despical.whackme.game.states.InGameState;
import dev.despical.whackme.game.states.RestartingState;
import dev.despical.whackme.game.states.WaitingState;
import dev.despical.whackme.scoreboard.ScoreboardManager;
import dev.despical.whackme.user.User;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.EnumMap;
import java.util.Map;

/**
 * A single running Whack Me session for an arena.
 *
 * @author Despical
 * <p>
 * Created at 13.07.2026
 */
public final class Game extends BukkitRunnable {

    private static final WhackMe plugin = WhackMe.getInstance();

    @Getter
    private final Arena arena;
    @Getter
    private final PointHandler pointHandler;
    @Getter
    private final BossBarManager bossBarManager;
    @Getter
    private final ScoreboardManager scoreboardManager;
    private final Map<GameState, GameStateHandler> states;

    @Getter
    private GameState state = GameState.INACTIVE;
    private GameStateHandler stateHandler;
    @Getter
    private User user;
    @Getter
    private int timer;
    private boolean scheduled;

    public Game(Arena arena) {
        this.arena = arena;
        this.pointHandler = new PointHandler(this);
        this.bossBarManager = new BossBarManager(arena);
        this.scoreboardManager = new ScoreboardManager(this);
        this.states = new EnumMap<>(GameState.class);
        states.put(GameState.WAITING, new WaitingState(this));
        states.put(GameState.IN_GAME, new InGameState(this));
        states.put(GameState.ENDING, new EndingState(this));
        states.put(GameState.RESTARTING, new RestartingState(this));
    }

    public void start() {
        if (!scheduled) {
            runTaskTimer(plugin, 20L, 20L);
            scheduled = true;
        }
        setState(GameState.WAITING);
    }

    @Override
    public void run() {
        if (stateHandler != null) {
            stateHandler.tick();
        }
    }

    public boolean join(User user, String messagePath) {
        if (state != GameState.WAITING || this.user != null) {
            return false;
        }

        this.user = user;
        states.get(GameState.WAITING).join(user);
        user.sendMessage(messagePath);
        if (setState(GameState.IN_GAME)) {
            return true;
        }

        plugin.getGameManager().finishGame(this, false, true, false);
        return false;
    }

    public void leave(User user) {
        if (this.user == null || !this.user.equals(user) || stateHandler == null) {
            return;
        }
        stateHandler.leave(user);
    }

    public void clearUser() {
        this.user = null;
    }

    public Player getPlayer() {
        return user == null ? null : user.getPlayer();
    }

    public boolean isPlaying(User user) {
        return this.user != null && this.user.equals(user);
    }

    public boolean isPlaying(Player player) {
        return user != null && user.getUUID().equals(player.getUniqueId());
    }

    public void setTimer(int timer) {
        this.timer = Math.max(timer, 0);
        scoreboardManager.update();
    }

    public boolean setState(GameState newState) {
        if (state == newState) {
            return true;
        }

        GameStateChangeEvent event = plugin.getEventManager().gameStateChange(this, state, newState);
        if (event.isCancelled()) {
            return false;
        }

        state = newState;
        stateHandler = states.get(newState);
        bossBarManager.update();
        scoreboardManager.update();

        if (plugin.getSignManager() != null) {
            plugin.getSignManager().updateSigns(arena);
        }

        if (stateHandler != null) {
            stateHandler.firstTick();
        }
        return true;
    }

    public boolean isState(GameState expected) {
        return state == expected;
    }

    public void shutdown() {
        pointHandler.clear();
        scoreboardManager.removeScoreboard();
        bossBarManager.removePlayer();
        cancel();
        scheduled = false;
        state = GameState.INACTIVE;
        stateHandler = null;
    }
}
