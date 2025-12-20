package dev.despical.whackme.handler;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import dev.despical.commons.number.NumberUtils;
import dev.despical.whackme.WhackMe;
import dev.despical.whackme.api.statistics.StatisticType;
import dev.despical.whackme.arena.Arena;
import dev.despical.whackme.user.User;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;

import static dev.despical.whackme.api.statistics.StatisticType.*;

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
            StatisticType statisticType = StatisticType.match(statName);

            if (statisticType == null) {
                return "No statistic like that: " + statName;
            }

            int position = NumberUtils.getInt(split[2], 1);
            Map.Entry<UUID, Integer> entry = plugin.getLeaderboardManager().orElseThrow(NullPointerException::new).getEntry(statisticType, position);

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

        switch (id.toLowerCase()) {
            case "all_arenas":
                return Integer.toString(plugin.getArenaRegistry().getArenas().size());
            case "ready_arenas":
                return Long.toString(plugin.getArenaRegistry().getArenas().stream().filter(Arena::isReady).count());
            case "online_players":
                return Long.toString(plugin.getArenaRegistry().getArenas().stream().filter(arena -> arena.getPlayer() != null).count());
            case "whacked_point_blocks":
                return PLUS_BLOCKS.from(user);
            case "whacked_minus_point_blocks":
                return MINUS_BLOCKS.from(user);
            case "whacked_block_rate":
                int minusBlocks = user.getStat(MINUS_BLOCKS), plusBlocks = user.getStat(PLUS_BLOCKS);
                return String.format("%.1f", (minusBlocks + plusBlocks == 0 ? 100 : ((double) plusBlocks / (minusBlocks + plusBlocks)) * 100D));
            case "record_score":
                return RECORD_SCORE.from(user);
            case "tours_played":
                return TOURS_PLAYED.from(user);
            case "longest_point_streak":
                return LONGEST_STREAK.from(user);
            case "local_score":
                return LOCAL_SCORE.from(user);
            case "local_point_streak":
                return LOCAL_STREAK.from(user);
            case "local_longest_point_streak":
                return LOCAL_LONGEST_STREAK.from(user);
            default:
                return handleArenaPlaceholderRequest(id);
        }
    }

    private String handleArenaPlaceholderRequest(String id) {
        String[] data = id.split(":");
        Arena arena = plugin.getArenaRegistry().getArena(data[0]);

        if (arena == null) return null;

        switch (data[1].toLowerCase()) {
            case "player_name":
                return arena.getPlayerName();
            case "point_blocks":
                return Integer.toString(arena.getPointBlocks().size());
            default:
                return null;
        }
    }
}
