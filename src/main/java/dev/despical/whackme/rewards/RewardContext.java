package dev.despical.whackme.rewards;

import dev.despical.whackme.arena.blocks.PointBlockType;
import dev.despical.whackme.game.Game;
import dev.despical.whackme.stats.Statistics;
import dev.despical.whackme.user.User;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * Immutable values captured when a reward trigger occurs.
 *
 * @author Despical
 * <p>
 * Created at 13.07.2026
 */
record RewardContext(Player player, Map<String, String> placeholders) {

    static RewardContext from(RewardType rewardType, Game game, PointBlockType blockType) {
        User user = game.getUser();
        Player player = game.getPlayer();

        if (user == null || player == null) {
            return null;
        }

        Map<String, String> placeholders = new HashMap<>(20);
        placeholders.put("%player%", player.getName());
        placeholders.put("%uuid%", player.getUniqueId().toString());
        placeholders.put("%arena%", game.getArena().getId());
        placeholders.put("%timer%", Integer.toString(game.getTimer()));
        placeholders.put("%score%", Integer.toString(user.getStatistic(Statistics.LOCAL_SCORE)));
        placeholders.put("%hit-streak%", Integer.toString(user.getStatistic(Statistics.LOCAL_HIT_STREAK)));
        placeholders.put("%max-streak%", Integer.toString(user.getStatistic(Statistics.LOCAL_LONGEST_HIT_STREAK)));
        placeholders.put("%green-blocks%", Integer.toString(user.getStatistic(Statistics.LOCAL_CORRECT_BLOCKS)));
        placeholders.put("%red-blocks%", Integer.toString(user.getStatistic(Statistics.LOCAL_WRONG_BLOCKS)));
        placeholders.put("%block-type%", blockType == null ? "NONE" : blockType.name());

        return new RewardContext(player, placeholders);
    }

    String format(String command) {
        String formatted = command;

        for (var placeholder : placeholders.entrySet()) {
            formatted = formatted.replace(placeholder.getKey(), placeholder.getValue());
        }

        return formatted;
    }
}
