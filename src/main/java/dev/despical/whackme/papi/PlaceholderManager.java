package dev.despical.whackme.papi;

import dev.despical.whackme.WhackMe;
import dev.despical.whackme.arena.Arena;
import dev.despical.whackme.arena.options.ArenaKeys;
import dev.despical.whackme.leaderboard.LeaderboardEntry;
import dev.despical.whackme.stats.StatisticType;
import dev.despical.whackme.stats.Statistics;
import dev.despical.whackme.user.User;
import lombok.RequiredArgsConstructor;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2022
 */
@RequiredArgsConstructor
public class PlaceholderManager extends PlaceholderExpansion {

    private final WhackMe plugin;

    @NotNull
    @Override
    public String getIdentifier() {
        return "wm";
    }

    @NotNull
    @Override
    public String getAuthor() {
        return "Despical";
    }

    @NotNull
    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Nullable
    @Override
    public String onPlaceholderRequest(Player player, @NotNull String id) {
        if (player == null) {
            return "";
        }

        switch (id) {
            case "arenas_total" -> {
                return Integer.toString(plugin.getArenaRegistry().getArenas().size());
            }
            case "arenas_ready" -> {
                return Long.toString(plugin.getArenaRegistry().getArenas().stream().filter(arena -> arena.getOption(ArenaKeys.READY)).count());
            }
            case "online_players" -> {
                return Long.toString(plugin.getArenaRegistry().getArenas().stream().filter(arena -> arena.getPlayer() != null).count());
            }
        }

        if (id.startsWith("arena:")) {
            return handleArenaPlaceholders(id);
        }

        if (id.startsWith("leaderboard:")) {
            return handleLeaderboardPlaceholders(id);
        }

        User user = plugin.getUserManager().getUser(player);
        if (user == null) {
            return "";
        }

        if (id.startsWith("stat:")) {
            return handleStatPlaceholder(user, id);
        }

        return switch (id.toLowerCase(Locale.ROOT)) {
            case "whacked_point_blocks" -> String.valueOf(user.getStatistic(Statistics.PLUS_BLOCKS));
            case "whacked_minus_point_blocks" -> String.valueOf(user.getStatistic(Statistics.MINUS_BLOCKS));
            case "whacked_block_rate" -> formatBlockRate(user);
            case "record_score" -> String.valueOf(user.getStatistic(Statistics.RECORD_SCORE));
            case "perfect_runs" -> String.valueOf(user.getStatistic(Statistics.PERFECT_RUNS));
            case "games_played" -> String.valueOf(user.getStatistic(Statistics.GAMES_PLAYED));
            case "tours_played" -> String.valueOf(user.getStatistic(Statistics.GAMES_PLAYED));
            case "longest_point_streak" -> String.valueOf(user.getStatistic(Statistics.LONGEST_HIT_STREAK));
            case "local_score" -> String.valueOf(user.getStatistic(Statistics.LOCAL_SCORE));
            case "local_point_streak" -> String.valueOf(user.getStatistic(Statistics.LOCAL_HIT_STREAK));
            case "local_longest_point_streak" -> String.valueOf(user.getStatistic(Statistics.LOCAL_LONGEST_HIT_STREAK));
            default -> null;
        };
    }

    private String handleLeaderboardPlaceholders(String id) {
        String[] split = id.split(":");
        if (split.length < 4) {
            return "";
        }

        String statName = split[1];
        var statistic = this.matchStatisticType(statName);

        if (statistic == null) {
            return "Invalid statistic: " + statName;
        }

        int position;
        try {
            position = Integer.parseInt(split[2]);
        } catch (NumberFormatException e) {
            return "Invalid position: " + split[2];
        }

        var leaderboard = plugin.getLeaderboardManager();
        if (leaderboard == null) return "";

        var board = leaderboard.getLeaderboard(statistic);
        LeaderboardEntry<Integer> entry = board == null ? null : board.getEntryAtPosition(position);

        String asked = split[3].toLowerCase(Locale.ROOT);

        if (entry == null) {
            return plugin.getChatManager().getRawString("placeholders.empty-" + (asked.equals("name") ? "position" : "value"));
        }

        return switch (asked) {
            case "name" -> entry.name();
            case "uuid" -> entry.uuid().toString();
            case "value" -> String.valueOf(entry.value());
            default -> "";
        };
    }

    private String handleArenaPlaceholders(String id) {
        String[] data = id.split(":");
        if (data.length < 3) return "";

        Arena arena = plugin.getArenaRegistry().getArena(data[1]);
        if (arena == null) return "No arena: " + data[1];

        return switch (data[2].toLowerCase(Locale.ROOT)) {
            case "player_name" -> arena.getPlayerName();
            case "point_blocks" -> Integer.toString(arena.getOption(ArenaKeys.POINT_BLOCKS).size());
            case "ready" -> Boolean.toString(arena.getOption(ArenaKeys.READY));
            case "state" -> arena.getGame() == null ? "inactive" : arena.getGame().getState().getPath();
            case "timer" -> arena.getGame() == null ? "0" : Integer.toString(arena.getGame().getTimer());
            case "score" -> arena.getGame() == null || arena.getGame().getUser() == null
                ? "0" : Integer.toString(arena.getGame().getUser().getStatistic(Statistics.LOCAL_SCORE));
            case "record_score" -> Integer.toString(arena.getOption(ArenaKeys.RECORD_SCORE));
            case "record_holder" -> arena.getOption(ArenaKeys.RECORD_HOLDER);
            default -> "";
        };
    }

    private String handleStatPlaceholder(User user, String id) {
        String[] data = id.split(":");
        if (data.length < 2) return "";

        StatisticType<Integer> statistic = matchStatisticType(data[1]);
        if (statistic == null) return "";

        return String.valueOf(user.getStatistic(statistic));
    }

    private String formatBlockRate(User user) {
        int plusBlocks = user.getStatistic(Statistics.PLUS_BLOCKS);
        int minusBlocks = user.getStatistic(Statistics.MINUS_BLOCKS);

        double rate = (minusBlocks + plusBlocks == 0) ? 100.0 : ((double) plusBlocks / (minusBlocks + plusBlocks)) * 100.0;
        return String.format(Locale.US, "%.1f", rate);
    }

    private StatisticType<Integer> matchStatisticType(String name) {
        return Statistics.getAllStats().stream()
            .filter(stat -> stat.getKey() != null && stat.getKey().equalsIgnoreCase(name))
            .filter(stat -> stat.getDefaultValue() instanceof Integer)
            .map(stat -> (StatisticType<Integer>) stat)
            .findFirst()
            .orElse(null);
    }
}
