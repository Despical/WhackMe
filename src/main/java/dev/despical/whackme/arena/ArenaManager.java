package dev.despical.whackme.arena;

import dev.despical.whackme.WhackMe;
import dev.despical.whackme.api.event.player.PlayerJoinAttemptEvent;
import dev.despical.whackme.api.event.player.PlayerLeaveGameEvent.LeaveReason;
import dev.despical.whackme.arena.options.ArenaKeys;
import dev.despical.whackme.chat.ChatManager;
import dev.despical.whackme.game.StopReason;
import dev.despical.whackme.game.Game;
import dev.despical.whackme.rewards.RewardType;
import dev.despical.whackme.user.User;
import dev.despical.whackme.util.ShutdownDetector;
import dev.despical.whackme.util.Var;
import org.bukkit.entity.Player;

/**
 * @author Despical
 * <p>
 * Created at 31.01.2024
 */
public class ArenaManager {

    private final WhackMe plugin;
    private final ChatManager chatManager;

    public ArenaManager(WhackMe plugin) {
        this.plugin = plugin;
        this.chatManager = plugin.getChatManager();
    }

    public boolean joinAttempt(User user, Arena arena) {
        return joinAttempt(user, arena, "game.start-message");
    }

    public boolean joinAttempt(User user, Arena arena, String joinMessagePath) {
        Player player = user.getPlayer();
        if (player == null) {
            return false;
        }

        Arena currentArena = plugin.getArenaRegistry().getArena(player);
        if (currentArena != null) {
            chatManager.sendMessage(player, currentArena.equals(arena) ? "already-playing" : "already-playing-other-arena");
            return false;
        }

        if (!arena.getOption(ArenaKeys.READY)) {
            chatManager.sendMessage(player, "arena-not-ready-yet");
            return false;
        }

        Game game = arena.getGame();
        if (game == null) {
            chatManager.sendMessage(player, "arena-not-ready-yet");
            return false;
        }

        if (game.getPlayer() != null) {
            chatManager.sendMessage(player, "someone-is-already-playing");
            return false;
        }

        if (!player.hasPermission("whackme.cooldown.bypass")) {
            double cooldown = user.getCooldown("play_again");
            if (cooldown > 0) {
                chatManager.sendMessage(player, "commands.wait-for-cooldown",
                    Var.of("%seconds%", (long) Math.ceil(cooldown)));
                return false;
            }
        }

        PlayerJoinAttemptEvent event = plugin.getEventManager().playerJoinAttempt(player, game);
        if (event.isCancelled()) {
            return false;
        }

        return plugin.getGameManager().joinPlayer(user, arena, joinMessagePath);
    }

    public void leaveAttempt(User user, LeaveReason reason) {
        Arena arena = user.getArena();
        if (arena == null) {
            user.sendMessage("not-playing");
            return;
        }

        Player player = user.getPlayer();
        if (player == null) {
            return;
        }

        plugin.getEventManager().playerLeave(player, arena.getGame(), reason);
        plugin.getRewardManager().dispatch(RewardType.GAME_QUIT, arena.getGame());
        plugin.getGameManager().leaveUser(user);
    }

    public void quitPlayer(User user, Arena arena) {
        if (arena == null) {
            return;
        }

        Player player = user.getPlayer();
        if (player == null) {
            return;
        }

        plugin.getEventManager().playerLeave(player, arena.getGame(), LeaveReason.QUIT);
        plugin.getRewardManager().dispatch(RewardType.GAME_QUIT, arena.getGame());
        plugin.getGameManager().quitUser(user);
    }

    public void handleDisable() {
        StopReason reason = resolveStopReason();

        for (Arena arena : plugin.getArenaRegistry().getArenas()) {
            plugin.getGameManager().stopArena(arena, reason);
            arena.stop();
        }
    }

    public void stopArena(Arena arena, StopReason reason) {
        plugin.getGameManager().stopArena(arena, reason);
    }

    private StopReason resolveStopReason() {
        return ShutdownDetector.isShutdown()
            ? StopReason.SERVER_SHUTDOWN
            : StopReason.SERVER_RELOAD;
    }
}
