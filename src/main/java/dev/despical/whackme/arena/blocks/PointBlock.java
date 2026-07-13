package dev.despical.whackme.arena.blocks;

import dev.despical.whackme.WhackMe;
import dev.despical.whackme.arena.Arena;
import dev.despical.whackme.arena.options.ArenaKeys;
import dev.despical.whackme.chat.ChatManager;
import dev.despical.whackme.sound.GameSound;
import dev.despical.whackme.option.BooleanOption;
import dev.despical.whackme.option.DoubleOption;
import dev.despical.whackme.option.IntOption;
import dev.despical.whackme.stats.Statistics;
import dev.despical.whackme.user.User;
import dev.despical.whackme.util.Utils;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * @author Despical
 * <p>
 * Created at 20.06.2022
 */
public class PointBlock extends BukkitRunnable {

    private static final WhackMe plugin = WhackMe.getInstance();
    private static final ChatManager chatManager = plugin.getChatManager();
    private static final Component PUNCH_ME = chatManager.getMessageComponent("point-blocks.punch-me");
    private static final Component DONT_PUNCH_ME = chatManager.getMessageComponent("point-blocks.dont-punch-me");
    private static final Component OUCH = chatManager.getMessageComponent("point-blocks.ouch");
    private static final boolean async = plugin.getOptions().isEnabled(BooleanOption.POINT_BLOCKS_RUN_ASYNC);
    final ArmorStand stand;
    private final PointHandler pointHandler;
    private final Arena arena;
    private final Location availableLocation;
    private final PointBlockType pointBlockType;
    private final double multiplier;
    private double y;
    private int waitedMs = plugin.getOptions().get(IntOption.POINT_BLOCK_WAIT_MILLISECONDS);
    private boolean forward = true, waitedAbove = true;
    private Listener listener;

    public PointBlock(PointHandler pointHandler, Location availableLocation) {
        this.pointHandler = pointHandler;
        this.arena = pointHandler.game().getArena();
        this.multiplier = Math.min(plugin.getOptions().get(DoubleOption.POINT_BLOCK_Y_MULTIPLIER), plugin.getOptions().get(DoubleOption.POINT_BLOCK_MAX_Y_MULTIPLIER));
        this.availableLocation = availableLocation;
        this.pointBlockType = decidePointBlockType();

        stand = (ArmorStand) availableLocation.getWorld().spawnEntity(availableLocation.clone().add(.5, -1.2, .5), EntityType.ARMOR_STAND);
        stand.getEquipment().setHelmet(getArenaItem(pointBlockType));
        stand.customName(getCustomName());
        stand.setCustomNameVisible(true);
        stand.setGravity(false);
        stand.setVisible(false);

        Utils.rotateGameBlocks(stand, availableLocation, arena.getOption(ArenaKeys.START_LOCATION), PUNCH_ME.equals(stand.customName()) ? "Punch-Me" : "Dont-Punch-Me");

        stand.setSilent(true);
        stand.setPersistent(false);

        pointHandler.getPointBlocks().add(this);

        registerEvents();
    }

    public void clear() {
        this.cancel();
        this.stand.remove();
        this.pointHandler.releaseAvailableLocation(availableLocation);

        HandlerList.unregisterAll(listener);
    }

    public void handleItself() {
        if (async) {
            this.runTaskTimerAsynchronously(plugin, 1L, 1L);
        } else {
            this.runTaskTimer(plugin, 1L, 1L);
        }
    }

    private PointBlockType decidePointBlockType() {
        int greenSize = (int) pointHandler.getPointBlocks().stream().filter(pointBlock -> PUNCH_ME.equals(pointBlock.stand.customName())).count(),
            redSize = (int) pointHandler.getPointBlocks().stream().filter(pointBlock -> DONT_PUNCH_ME.equals(pointBlock.stand.customName())).count();

        if (greenSize > redSize) {
            return PointBlockType.RED_BLOCK;
        } else if (greenSize == redSize) {
            return PointBlockType.GREEN_BLOCK;
        }

        return PointBlockType.GREEN_BLOCK;
    }

    private Component getCustomName() {
        return pointBlockType == PointBlockType.GREEN_BLOCK ? PUNCH_ME : DONT_PUNCH_ME;
    }

    private ItemStack getArenaItem(PointBlockType type) {
        return arena.getOption(type.getArenaOption()).clone();
    }

    private void registerEvents() {
        plugin.getServer().getPluginManager().registerEvents(listener = new Listener() {

            @EventHandler
            public void onArmorStandManipulate(PlayerArmorStandManipulateEvent event) {
                Player player = event.getPlayer();

                if (!arena.isPlaying(player)) return;

                ArmorStand armorStand = event.getRightClicked();

                if (!armorStand.equals(stand)) return;

                event.setCancelled(true);
            }

            @EventHandler
            public void onArmorStandDamage(EntityDamageByEntityEvent event) {
                if (!(event.getDamager() instanceof Player player)) return;
                if (!(event.getEntity() instanceof ArmorStand armorStand)) return;
                if (!arena.isPlaying(player)) return;
                if (!armorStand.equals(stand)) return;

                User user = plugin.getUserManager().getUser(player);
                Component name = stand.customName();

                if (name == null) return;
                if (name.equals(OUCH)) {
                    plugin.getSoundManager().play(player, GameSound.OUCH);
                    event.setCancelled(true);
                    return;
                }

                if (name.equals(PUNCH_ME)) {
                    user.addStat(Statistics.LOCAL_SCORE, 1);
                    user.addStat(Statistics.LOCAL_HIT_STREAK, 1);
                    user.addStat(Statistics.LOCAL_CORRECT_BLOCKS, 1);
                    user.addStat(Statistics.PLUS_BLOCKS, 1);

                    int localStreak = user.getStatistic(Statistics.LOCAL_HIT_STREAK);

                    if (localStreak > user.getStatistic(Statistics.LOCAL_LONGEST_HIT_STREAK)) {
                        user.setStatistic(Statistics.LOCAL_LONGEST_HIT_STREAK, localStreak);
                    }

                    plugin.getSoundManager().play(player, GameSound.POINT);
                } else if (name.equals(DONT_PUNCH_ME)) {
                    user.setStatistic(Statistics.LOCAL_SCORE, Math.max(0, user.getStatistic(Statistics.LOCAL_SCORE) - 1));
                    user.addStat(Statistics.MINUS_BLOCKS, 1);
                    user.addStat(Statistics.LOCAL_WRONG_BLOCKS, 1);
                    user.setStatistic(Statistics.LOCAL_HIT_STREAK, 0);

                    plugin.getSoundManager().play(player, GameSound.MINUS_POINT);
                }

                pointHandler.game().getScoreboardManager().update();

                event.setCancelled(true);

                stand.getEquipment().setHelmet(getArenaItem(PointBlockType.GRAY_BLOCK));
                stand.customName(OUCH);

                Utils.rotateGameBlocks(stand, availableLocation, arena.getOption(ArenaKeys.START_LOCATION), "Ouch");
            }
        }, plugin);
    }

    @Override
    public void run() {
        if (forward) {
            y += multiplier;

            if (y > .85) {
                forward = false;
                waitedAbove = false;
                return;
            }

            handleEntityTeleportation(stand.getLocation().clone().add(0, multiplier, 0));
        } else {
            if (!waitedAbove) {
                waitedMs--;

                if (waitedMs == 0) waitedAbove = true;
            }

            if (waitedMs != 0) return;

            y -= multiplier;

            if (y < -0.6) {
                forward = true;

                cancel();
                handleEntityRemoval();

                pointHandler.getPointBlocks().remove(this);
                pointHandler.releaseAvailableLocation(availableLocation);

                HandlerList.unregisterAll(listener);
                return;
            }

            handleEntityTeleportation(stand.getLocation().clone().subtract(0, multiplier, 0));
        }
    }

    private void handleEntityRemoval() {
        if (async) {
            plugin.getServer().getScheduler().runTask(plugin, stand::remove);
        } else {
            stand.remove();
        }
    }

    private void handleEntityTeleportation(Location destination) {
        if (async) {
            stand.teleportAsync(destination);
        } else {
            stand.teleport(destination);
        }
    }
}
