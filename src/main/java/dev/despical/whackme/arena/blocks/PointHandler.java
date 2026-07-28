package dev.despical.whackme.arena.blocks;

import dev.despical.commons.string.StringFormatUtils;
import dev.despical.whackme.WhackMe;
import dev.despical.whackme.arena.Arena;
import dev.despical.whackme.arena.options.ArenaKeys;
import dev.despical.whackme.game.Game;
import dev.despical.whackme.game.GameState;
import dev.despical.whackme.stats.Statistics;
import dev.despical.whackme.user.User;
import dev.despical.whackme.util.Var;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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
    private final PointBlockFactory pointBlockFactory;
    private BukkitTask task;

    public PointHandler(Game game) {
        this.game = game;
        this.arena = game.getArena();
        this.plugin = WhackMe.getInstance();
        this.availableLocations = new ArrayList<>();
        this.pointBlockFactory = new PointBlockFactory(plugin, arena);
    }

    private void tick() {
        if (!game.isState(GameState.IN_GAME)) {
            return;
        }

        Player player = arena.getPlayer();
        if (player == null) return;

        sendActionBar(player);

        int size = getPointBlocks().size();
        int maximumPoints = Math.min(arena.getOption(ArenaKeys.MAXIMUM_POINTS), getLocationCapacity());

        if (maximumPoints > 0 && size < maximumPoints && size < random(maximumPoints + 1)) {
            spawnPointBlock();
        }
    }

    private int random(int max) {
        int min = Math.clamp(arena.getOption(ArenaKeys.MINIMUM_POINTS), 0, max);

        return min == max ? min : ThreadLocalRandom.current().nextInt(min, max);
    }

    private void spawnPointBlock() {
        Location location = reserveAvailableLocation();
        if (location != null) {
            PointBlock pointBlock = pointBlockFactory.create(this, location);
            getPointBlocks().add(pointBlock);
            pointBlock.start();
        }
    }

    private void sendActionBar(Player player) {
        String message = plugin.getChatManager().getRawString("game.action-bar");
        if (message.isEmpty()) return;

        int timer = game.getTimer();
        User user = plugin.getUserManager().getUser(player);
        int correctBlocks = user.getStatistic(Statistics.LOCAL_CORRECT_BLOCKS);
        int wrongBlocks = user.getStatistic(Statistics.LOCAL_WRONG_BLOCKS);
        int totalBlocks = correctBlocks + wrongBlocks;
        double successRate = totalBlocks == 0 ? 100D : (correctBlocks * 100D) / totalBlocks;

        Var[] vars = {
            Var.of("%timer%", timer),
            Var.of("%timer_formatted%", StringFormatUtils.formatIntoMMSS(timer)),
            Var.of("%score%", user.getStatistic(Statistics.LOCAL_SCORE)),
            Var.of("%hit_streak%", user.getStatistic(Statistics.LOCAL_HIT_STREAK)),
            Var.of("%success_rate%", String.format(Locale.US, "%.1f", successRate)),
        };

        plugin.getChatManager().sendRawActionBar(player, message, vars);
    }

    public void start() {
        if (task != null) {
            return;
        }

        resetAvailableLocations();
        long period = Math.max(1, arena.getOption(ArenaKeys.POINT_BLOCK_TICKS));
        task = plugin.getServer().getScheduler().runTaskTimer(plugin, this::tick, 8L, period);
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
        arena.getOption(ArenaKeys.PORTAL_LOCATIONS).stream()
            .distinct()
            .forEach(availableLocations::add);
    }

    void release(PointBlock pointBlock, Location location) {
        getPointBlocks().remove(pointBlock);
        releaseAvailableLocation(location);
    }

    private synchronized int getLocationCapacity() {
        return availableLocations.size() + getPointBlocks().size();
    }
}
