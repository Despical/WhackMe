package dev.despical.whackme.arena.blocks;

import dev.despical.commons.messages.ActionBar;
import dev.despical.commons.string.StringFormatUtils;
import dev.despical.whackme.WhackMe;
import dev.despical.whackme.api.statistics.StatisticType;
import dev.despical.whackme.api.statistics.StatsStorage;
import dev.despical.whackme.arena.Arena;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Despical
 * <p>
 * Created at 21.06.2022
 */
public class PointHandler extends BukkitRunnable {

    private final Arena arena;
    private final WhackMe plugin;

    public PointHandler(Arena arena) {
        this.arena = arena;
        this.plugin = WhackMe.getInstance();
    }

    @Override
    public void run() {
        Player player = arena.getPlayer();

        if (player == null) return;

        sendActionBar(player);

        int size = arena.getPointBlocks().size();
        int maximumPoints = arena.getMaximumPoints();

        if (size <= maximumPoints && size < random(maximumPoints + 1)) {
            new PointBlock(arena).handleItself();
        }
    }

    private int random(int max) {
        int min = arena.getMinimumPoints();

        return min == max ? min : ThreadLocalRandom.current().nextInt(min, max);
    }

    private void sendActionBar(Player player) {
        String message = plugin.getChatManager().message("in_game.action_bar");

        if (message.isEmpty()) return;

        message = message.replace("%player%", player.getName());
        message = message.replace("%score%", Integer.toString(StatsStorage.getUserStats(player, StatisticType.LOCAL_SCORE)));
        message = message.replace("%timer%", StringFormatUtils.formatIntoMMSS(arena.getTimer()));

        ActionBar.sendActionBar(player, message);
    }

    public void handleTask() {
        runTaskTimer(plugin, 8L, plugin.getConfigPreferences().getTicks());
    }
}
