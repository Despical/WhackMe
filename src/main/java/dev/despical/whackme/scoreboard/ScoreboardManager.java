package dev.despical.whackme.scoreboard;

import dev.despical.commons.configuration.ConfigUtils;
import dev.despical.commons.scoreboard.Scoreboard;
import dev.despical.commons.scoreboard.ScoreboardHandler;
import dev.despical.commons.scoreboard.ScoreboardLib;
import dev.despical.commons.scoreboard.common.Entry;
import dev.despical.commons.scoreboard.common.EntryBuilder;
import dev.despical.whackme.WhackMe;
import dev.despical.whackme.game.Game;
import dev.despical.whackme.option.BooleanOption;
import dev.despical.whackme.scoreboard.formatter.GlobalFormatter;
import dev.despical.whackme.user.User;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * @author Despical
 * <p>
 * Created at 13.07.2026
 */
public final class ScoreboardManager {

    private static final WhackMe plugin = WhackMe.getInstance();

    private String title;
    private List<String> lines;

    private Scoreboard scoreboard;

    private final Game game;

    public ScoreboardManager(Game game) {
        this.game = game;
        this.loadContent();
    }

    public void create(Player player) {
        if (player == null) {
            return;
        }

        removeScoreboard();

        if (!BooleanOption.SCOREBOARD_ENABLED.value()) {
            return;
        }

        scoreboard = ScoreboardLib.createScoreboard(player);
        scoreboard.setHandler(new ScoreboardHandler() {

            @Override
            public Component getTitle(Player player) {
                return plugin.getChatManager().parseMessage(title);
            }

            @Override
            public List<Entry> getEntries(Player player) {
                return getLines();
            }
        });

        scoreboard.disableAutoUpdate();
        scoreboard.activate();
        scoreboard.update();
    }

    private List<Entry> getLines() {
        EntryBuilder builder = new EntryBuilder();
        User user = game.getUser();

        for (String line : lines) {
            builder.next(formatLine(line, user));
        }

        return builder.build();
    }

    private String formatLine(String line, User user) {
        return GlobalFormatter.INSTANCE.format(user, game, line);
    }

    public void update() {
        if (scoreboard != null) {
            scoreboard.update();
        }
    }

    public void removeScoreboard() {
        if (scoreboard == null) {
            return;
        }

        Player player = game.getPlayer();
        scoreboard.deactivate();
        scoreboard = null;

        if (player != null) {
            player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        }
    }

    public void loadContent() {
        FileConfiguration config = ConfigUtils.getConfig(plugin, "scoreboard");

        title = config.getString("title");
        lines = config.getStringList("lines");
    }
}
