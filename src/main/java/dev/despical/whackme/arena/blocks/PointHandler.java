package dev.despical.whackme.arena.blocks;

import dev.despical.commons.string.StringFormatUtils;
import dev.despical.whackme.WhackMe;
import dev.despical.whackme.arena.Arena;
import dev.despical.whackme.arena.options.ArenaKeys;
import dev.despical.whackme.game.Game;
import dev.despical.whackme.game.GameState;
import dev.despical.whackme.option.IntOption;
import dev.despical.whackme.stats.Statistics;
import dev.despical.whackme.user.User;
import dev.despical.whackme.util.Var;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Despical
 * <p>
 * Created at 21.06.2022
 */
public class PointHandler {

    private final Game game;
    private final Arena arena;
    private final WhackMe plugin;
    private final List<Location> availableLocations;
    private BukkitTask task;

    public PointHandler(Game game) {
        this.game = game;
        this.arena = game.getArena();
        this.plugin = WhackMe.getInstance();
        this.availableLocations = new ArrayList<>();
    }

    private void tick() {
        if (!game.isState(GameState.IN_GAME)) {
            return;
        }

        Player player = arena.getPlayer();
        if (player == null) return;

        sendActionBar(player);

        int size = getPointBlocks().size();
        int maximumPoints = arena.getOption(ArenaKeys.MAXIMUM_POINTS);

        if (size < maximumPoints && size < random(maximumPoints + 1)) {
            new PointBlock(this).handleItself();
        }
    }

    private int random(int max) {
        int min = arena.getOption(ArenaKeys.MINIMUM_POINTS);

        return min == max ? min : ThreadLocalRandom.current().nextInt(min, max);
    }

    private void sendActionBar(Player player) {
        String message = plugin.getChatManager().getRawString("game.action-bar");
        if (message.isEmpty()) return;

        int timer = game.getTimer();
        User user = plugin.getUserManager().getUser(player);

        Var[] vars = {
            Var.of("%timer%", timer),
            Var.of("%timer_formatted%", StringFormatUtils.formatIntoMMSS(timer)),
            Var.of("%score%", user.getStatistic(Statistics.LOCAL_SCORE)),
            Var.of("%hit_streak%", user.getStatistic(Statistics.LOCAL_HIT_STREAK)),
        };

        plugin.getChatManager().sendRawActionBar(player, message, vars);
    }

    public void start() {
        if (task != null) {
            return;
        }

        resetAvailableLocations();
        task = plugin.getServer().getScheduler().runTaskTimer(plugin, this::tick, 8L, IntOption.POINT_BLOCK_TICKS.value());
    }

    public void stop() {
        if (task == null) {
            return;
        }

        task.cancel();
        task = null;
    }

    public void clear() {
        stop();

        List<PointBlock> pointBlocks = getPointBlocks();
        new ArrayList<>(pointBlocks).forEach(PointBlock::clear);
        pointBlocks.clear();
        resetAvailableLocations();
    }

    List<PointBlock> getPointBlocks() {
        return arena.getOption(ArenaKeys.POINT_BLOCKS);
    }

    Game game() {
        return game;
    }

    synchronized Location reserveAvailableLocation() {
        if (availableLocations.isEmpty()) {
            return null;
        }

        return availableLocations.remove(ThreadLocalRandom.current().nextInt(availableLocations.size()));
    }

    synchronized void releaseAvailableLocation(Location location) {
        if (location != null && !availableLocations.contains(location)) {
            availableLocations.add(location);
        }
    }

    private synchronized void resetAvailableLocations() {
        availableLocations.clear();
        availableLocations.addAll(arena.getOption(ArenaKeys.PORTAL_LOCATIONS));
    }
}
