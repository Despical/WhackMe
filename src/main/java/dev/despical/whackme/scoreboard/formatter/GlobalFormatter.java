package dev.despical.whackme.scoreboard.formatter;

import dev.despical.commons.string.StringFormatUtils;
import dev.despical.whackme.arena.Arena;
import dev.despical.whackme.arena.options.ArenaKeys;
import dev.despical.whackme.stats.Statistics;
import dev.despical.whackme.util.Utils;
import dev.despical.whackme.util.Var;
import lombok.experimental.UtilityClass;

/**
 * @author Despical
 * <p>
 * Created at 13.07.2026
 */
@UtilityClass
public final class GlobalFormatter {

    private static final String DATE = StringFormatUtils.formatToday();

    public static final LineFormatter INSTANCE = (user, game, line) -> {
        Arena arena = game.getArena();
        int timer = game.getTimer();
        int correctBlocks = user.getStatistic(Statistics.LOCAL_CORRECT_BLOCKS);
        int wrongBlocks = user.getStatistic(Statistics.LOCAL_WRONG_BLOCKS);
        int totalBlocks = correctBlocks + wrongBlocks;
        double successRate = totalBlocks == 0 ? 100D : (correctBlocks * 100D) / totalBlocks;

        return Utils.format(line,
            Var.ofPlayer(user),
            Var.of("%date%", DATE),
            Var.of("%arena%", arena.getId()),
            Var.of("%timer%", timer),
            Var.of("%formatted_timer%", "%02d:%02d".formatted(timer / 60, timer % 60)),
            Var.of("%score%", user.getStatistic(Statistics.LOCAL_SCORE)),
            Var.of("%hit_streak%", user.getStatistic(Statistics.LOCAL_HIT_STREAK)),
            Var.of("%record_score%", arena.getOption(ArenaKeys.RECORD_SCORE)),
            Var.of("%record_holder%", arena.getOption(ArenaKeys.RECORD_HOLDER)),
            Var.of("%success_rate%", "%.1f".formatted(successRate))
        );
    };
}
