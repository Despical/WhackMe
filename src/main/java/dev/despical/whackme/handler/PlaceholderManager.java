package dev.despical.whackme.handler;

import dev.despical.whackme.stat.LocalStatistic;
import dev.despical.whackme.stat.Statistic;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import dev.despical.commons.number.NumberUtils;
import dev.despical.whackme.WhackMe;
import dev.despical.whackme.stat.StatisticType;
import dev.despical.whackme.arena.Arena;
import dev.despical.whackme.user.User;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2022
 */
public class PlaceholderManager extends PlaceholderExpansion {

    private final WhackMe plugin;

    public PlaceholderManager(WhackMe plugin) {
        this.plugin = plugin;

        register();
    }

    @Override
    public boolean persist() {
        return true;
    }

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
    public String onPlaceholderRequest(Player player, @NotNull String id) {
        if (player == null) return null;

        if (id.startsWith("top:")) {
            String[] split = id.split(":");

            if (split.length != 4) {
                return null;
            }

            String statName = split[1];
            var statistic = this.matchStatisticType(statName);

            if (statistic == null) {
                return "No statistic like that: " + statName;
            }

            int position = NumberUtils.getInt(split[2], 1);
            Map.Entry<UUID, Integer> entry = plugin.getLeaderboardManager().orElseThrow(NullPointerException::new).getEntry(statistic, position);

            boolean isName = "name".equals(split[3]);

            if (entry == null) {
                return plugin.getChatManager().message("Placeholders.Empty-" + (isName ? "Position" : "Value"));
            }

            if (isName) {
                return plugin.getServer().getOfflinePlayer(entry.getKey()).getName();
            }

            return Integer.toString(entry.getValue());
        }

        User user = plugin.getUserManager().getUser(player);

        return switch (id.toLowerCase()) {
            case "all_arenas" -> Integer.toString(plugin.getArenaRegistry().getArenas().size());
            case "ready_arenas" ->
                Long.toString(plugin.getArenaRegistry().getArenas().stream().filter(Arena::isReady).count());
            case "online_players" ->
                Long.toString(plugin.getArenaRegistry().getArenas().stream().filter(arena -> arena.getPlayer() != null).count());
            case "whacked_point_blocks" -> getStat(user, Statistic.PLUS_BLOCKS);
            case "whacked_minus_point_blocks" -> getStat(user, Statistic.MINUS_BLOCKS);
            case "whacked_block_rate" -> {
                int minusBlocks = user.getStat(Statistic.MINUS_BLOCKS), plusBlocks = user.getStat(dev.despical.whackme.stat.Statistic.PLUS_BLOCKS);
                yield String.format("%.1f", (minusBlocks + plusBlocks == 0 ? 100 : ((double) plusBlocks / (minusBlocks + plusBlocks)) * 100D));
            }
            case "record_score" -> getStat(user, Statistic.RECORD_SCORE);
            case "tours_played" -> getStat(user, Statistic.TOURS_PLAYED);
            case "longest_point_streak" -> getStat(user, Statistic.LONGEST_STREAK);
            case "local_score" -> getStat(user, LocalStatistic.SCORE);
            case "local_point_streak" -> getStat(user, LocalStatistic.STREAK);
            case "local_longest_point_streak" -> getStat(user, LocalStatistic.LONGEST_STREAK);
            default -> handleArenaPlaceholderRequest(id);
        };
    }

    private String getStat(User user, StatisticType statisticType) {
        var stat = user.getStat(statisticType);
        return Integer.toString(stat);
    }

    private String handleArenaPlaceholderRequest(String id) {
        String[] data = id.split(":");
        Arena arena = plugin.getArenaRegistry().getArena(data[0]);

        if (arena == null) return null;

        return switch (data[1].toLowerCase()) {
            case "player_name" -> arena.getPlayerName();
            case "point_blocks" -> Integer.toString(arena.getPointBlocks().size());
            default -> null;
        };
    }

    private Statistic matchStatisticType(String name) {
        return Stream.of(Statistic.values())
            .filter(statisticType -> statisticType.getName().equals(name))
            .findFirst()
            .orElse(null);
    }
}
