package dev.despical.whackme.arena.blocks;

import dev.despical.whackme.WhackMe;
import dev.despical.whackme.arena.Arena;
import dev.despical.whackme.sound.GameSound;
import dev.despical.whackme.rewards.RewardType;
import dev.despical.whackme.stats.Statistics;
import dev.despical.whackme.user.User;
import org.bukkit.entity.Player;

/**
 * @author Despical
 * <p>
 * Created at 14.07.2026
 */
final class PointBlockScoreService {

    private final WhackMe plugin;
    private final Arena arena;

    PointBlockScoreService(WhackMe plugin, Arena arena) {
        this.plugin = plugin;
        this.arena = arena;
    }

    void apply(Player player, PointBlockType type) {
        User user = plugin.getUserManager().getUser(player);

        if (type == PointBlockType.GREEN_BLOCK) {
            rewardCorrectHit(player, user);
        } else if (type == PointBlockType.RED_BLOCK) {
            penalizeWrongHit(player, user);
        }

        plugin.getRewardManager().dispatch(toRewardType(type), arena.getGame(), type);
    }

    private RewardType toRewardType(PointBlockType type) {
        return switch (type) {
            case GREEN_BLOCK -> RewardType.GREEN_BLOCK_STEP;
            case RED_BLOCK -> RewardType.RED_BLOCK_STEP;
            case GRAY_BLOCK -> RewardType.GRAY_BLOCK_STEP;
        };
    }

    private void rewardCorrectHit(Player player, User user) {
        user.addStat(Statistics.LOCAL_SCORE, 1);
        user.addStat(Statistics.LOCAL_HIT_STREAK, 1);
        user.addStat(Statistics.LOCAL_CORRECT_BLOCKS, 1);
        user.addStat(Statistics.PLUS_BLOCKS, 1);

        int localStreak = user.getStatistic(Statistics.LOCAL_HIT_STREAK);
        if (localStreak > user.getStatistic(Statistics.LOCAL_LONGEST_HIT_STREAK)) {
            user.setStatistic(Statistics.LOCAL_LONGEST_HIT_STREAK, localStreak);
        }

        plugin.getSoundManager().play(player, GameSound.POINT);
    }

    private void penalizeWrongHit(Player player, User user) {
        user.setStatistic(Statistics.LOCAL_SCORE, Math.max(0, user.getStatistic(Statistics.LOCAL_SCORE) - 1));
        user.addStat(Statistics.MINUS_BLOCKS, 1);
        user.addStat(Statistics.LOCAL_WRONG_BLOCKS, 1);
        user.setStatistic(Statistics.LOCAL_HIT_STREAK, 0);

        plugin.getSoundManager().play(player, GameSound.MINUS_POINT);
    }
}
