package dev.despical.whackme.game;

import dev.despical.whackme.WhackMe;
import dev.despical.whackme.arena.Arena;
import dev.despical.whackme.arena.options.ArenaKeys;
import dev.despical.whackme.chat.ChatManager;
import dev.despical.whackme.option.IntOption;
import dev.despical.whackme.sound.GameSound;
import dev.despical.whackme.stats.Statistics;
import dev.despical.whackme.user.User;
import dev.despical.whackme.util.Utils;
import dev.despical.whackme.util.Var;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Locale;

/**
 * @author Despical
 * <p>
 * Created at 13.07.2026
 */
public final class GameManager {

    private final WhackMe plugin;
    private final ChatManager chatManager;

    public GameManager(WhackMe plugin) {
        this.plugin = plugin;
        this.chatManager = plugin.getChatManager();
    }

    public boolean joinPlayer(User user, Arena arena, String joinMessagePath) {
        Game game = arena.getGame();
        return game != null && game.join(user, joinMessagePath);
    }

    public boolean preparePlayer(Game game, User user) {
        Player player = user.getPlayer();
        if (player == null) {
            return false;
        }

        if (!plugin.getPlayerInventoryManager().save(player)) {
            plugin.getLogger().severe("Could not save inventory for " + player.getName() + "; refusing to clear it.");
            return false;
        }

        player.getInventory().clear();

        user.resetTemporaryStats();
        Utils.resetPlayerAttributes(player);

        game.getScoreboardManager().create(player);
        game.getBossBarManager().update();

        plugin.getRadio().addArena(game.getArena());
        plugin.getSignManager().updateSigns(game.getArena());
        return true;
    }

    public void leaveUser(User user) {
        Game game = getGame(user);
        if (game != null) {
            game.leave(user);
        }
    }

    public void quitUser(User user) {
        Game game = getGame(user);
        if (game == null) {
            return;
        }

        finishGame(game, true, false, false, false);
        game.setState(GameState.RESTARTING);
    }

    public void stopGame(Game game, StopReason reason) {
        if (game == null || game.getUser() == null) {
            return;
        }

        User user = game.getUser();
        Player player = user.getPlayer();

        if (player != null) {
            plugin.getEventManager().playerLeave(player, game, reason.getLeaveReason());
        }

        forceStop(game, reason);

        plugin.getEventManager().gameStop(game, reason, List.of(user.getUUID()));
        game.setState(GameState.RESTARTING);
    }

    public void stopArena(Arena arena, StopReason reason) {
        stopGame(arena.getGame(), reason);
    }

    public void finishGame(Game game, boolean affectStats, boolean teleportToEnd, boolean sendFinishMessage) {
        finishGame(game, affectStats, teleportToEnd, sendFinishMessage, true);
    }

    private void finishGame(Game game, boolean affectStats, boolean teleportToEnd, boolean sendFinishMessage, boolean restoreInventory) {
        User user = game.getUser();
        Player player = game.getPlayer();

        if (user == null) {
            return;
        }

        Arena arena = game.getArena();
        if (affectStats && player != null) {
            applyRunResults(arena, user, player, sendFinishMessage);
        }

        plugin.getDatabase().saveData(user);
        plugin.getLeaderboardManager().refreshAllLeaderboards(List.of(user));

        cleanupPlayer(game, player, teleportToEnd, restoreInventory);

        user.resetTemporaryStats();
        game.clearUser();

        plugin.getSignManager().updateSigns(arena);
    }

    private void applyRunResults(Arena arena, User user, Player player, boolean sendFinishMessage) {
        int score = user.getStatistic(Statistics.LOCAL_SCORE);
        int arenaRecord = arena.getOption(ArenaKeys.RECORD_SCORE);
        int previousPersonalRecord = user.getStatistic(Statistics.RECORD_SCORE);

        boolean globalRecord = score > arenaRecord;
        boolean personalBest = score > previousPersonalRecord;

        Var[] resultVars = createResultVars(user, score, previousPersonalRecord);

        if (globalRecord) {
            arena.setOption(ArenaKeys.RECORD_HOLDER, player.getName());
            arena.setOption(ArenaKeys.RECORD_SCORE, score);

            chatManager.sendCenteredMessage(player,
                personalBest ? "game.global-record-broken-and-pr" : "game.global-record-broken",
                resultVars
            );
            plugin.getSoundManager().play(player, GameSound.RECORD_BROKEN);
        }

        if (personalBest) {
            user.setStatistic(Statistics.RECORD_SCORE, score);

            if (sendFinishMessage && !globalRecord) {
                chatManager.sendCenteredMessage(player, "game.new-record", resultVars);
            }
        } else if (sendFinishMessage && !globalRecord) {
            chatManager.sendCenteredMessage(player, "game.finished", resultVars);
            plugin.getSoundManager().play(player, GameSound.GAME_FINISHED);
        }

        int localStreak = user.getStatistic(Statistics.LOCAL_LONGEST_HIT_STREAK);
        user.setStatisticIfHigher(Statistics.LONGEST_HIT_STREAK, localStreak);
        user.addStat(Statistics.GAMES_PLAYED, 1);

        if (score > 0 && user.getStatistic(Statistics.LOCAL_WRONG_BLOCKS) == 0) {
            user.addStat(Statistics.PERFECT_RUNS, 1);
        }

        user.setCooldown("play_again", plugin.getOptions().get(IntOption.GAME_COOLDOWN));
    }

    private Var[] createResultVars(User user, int score, int previousPersonalRecord) {
        int correctBlocks = user.getStatistic(Statistics.LOCAL_CORRECT_BLOCKS);
        int wrongBlocks = user.getStatistic(Statistics.LOCAL_WRONG_BLOCKS);
        int totalBlocks = correctBlocks + wrongBlocks;

        double successRate = totalBlocks == 0 ? 0D : (correctBlocks * 100D) / totalBlocks;
        String missStatus;

        if (totalBlocks == 0) {
            missStatus = "<#B0BEC5>No blocks were hit this round.";
        } else if (wrongBlocks == 0) {
            missStatus = "<#00E676><bold>PERFECT RUN</bold> <gray>— No misses!";
        } else {
            missStatus = "<#FF5252><bold>%d MISS%s</bold> <gray>— Keep an eye on the red blocks.".formatted(wrongBlocks, wrongBlocks == 1 ? "" : "ES");
        }

        return new Var[]{
            Var.of("%points%", score),
            Var.of("%max_streak%", user.getStatistic(Statistics.LOCAL_LONGEST_HIT_STREAK)),
            Var.of("%success_rate%", String.format(Locale.US, "%.1f", successRate)),
            Var.of("%green_blocks%", correctBlocks),
            Var.of("%red_blocks%", wrongBlocks),
            Var.of("%previous_record%", previousPersonalRecord),
            Var.of("%personal_record%", Math.max(previousPersonalRecord, score)),
            Var.of("%miss_status%", missStatus)
        };
    }

    private void forceStop(Game game, StopReason reason) {
        Player player = game.getPlayer();

        if (player != null) {
            cleanupPlayer(game, player, true, true);
            chatManager.sendCenteredMessage(player, reason.getMessagePath());
        }

        User user = game.getUser();
        if (user != null) {
            user.resetTemporaryStats();
        }

        game.clearUser();
    }

    private void cleanupPlayer(Game game, Player player, boolean teleportToEnd) {
        cleanupPlayer(game, player, teleportToEnd, true);
    }

    private void cleanupPlayer(Game game, Player player, boolean teleportToEnd, boolean restoreInventory) {
        Arena arena = game.getArena();

        game.getPointHandler().clear();
        game.getScoreboardManager().removeScoreboard();
        game.getBossBarManager().removePlayer();

        if (player == null) {
            plugin.getRadio().removeArena(arena);
            return;
        }

        if (restoreInventory) {
            player.getInventory().clear();
            plugin.getPlayerInventoryManager().restore(player);
        }

        if (teleportToEnd && arena.getOption(ArenaKeys.END_LOCATION) != null) {
            player.teleport(arena.getOption(ArenaKeys.END_LOCATION));
        }

        plugin.getRadio().removePlayer(arena, player);
        plugin.getRadio().removeArena(arena);
    }

    private Game getGame(User user) {
        Arena arena = user.getArena();
        return arena == null ? null : arena.getGame();
    }

    public List<Game> getGames() {
        return plugin.getArenaRegistry().getArenas().stream()
            .filter(Arena::isGameNonnull)
            .map(Arena::getGame)
            .toList();
    }

    public void reload() {
        getGames().forEach(game -> {
            game.getScoreboardManager().loadContent();
            game.getBossBarManager().update();
        });
    }
}
