package dev.despical.whackme.scoreboard.formatter;

import dev.despical.whackme.game.Game;
import dev.despical.whackme.user.User;

/**
 * @author Despical
 * <p>
 * Created at 13.07.2026
 */
@FunctionalInterface
public interface LineFormatter {

    String format(User user, Game game, String line);
}
