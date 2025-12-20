package dev.despical.whackme.arena.blocks;

import dev.despical.commons.reflection.XReflection;
import dev.despical.whackme.WhackMe;
import dev.despical.whackme.arena.Arena;
import dev.despical.whackme.arena.options.ArenaOption;
import dev.despical.whackme.handler.ChatManager;
import dev.despical.whackme.handler.SoundManager;
import dev.despical.whackme.handler.rewards.Reward;
import dev.despical.whackme.user.User;
import dev.despical.whackme.util.Utils;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.scheduler.BukkitRunnable;

import static dev.despical.whackme.api.statistics.StatisticType.*;

/**
 * @author Despical
 * <p>
 * Created at 20.06.2022
 */
public class PointBlock extends BukkitRunnable {

    private static final WhackMe plugin = WhackMe.getInstance();
    private static final ChatManager chatManager = plugin.getChatManager();
    private static final String PUNCH_ME = chatManager.message("point_blocks.punch_me"), DONT_PUNCH_ME = chatManager.message("point_blocks.dont_punch_me"), OUCH = chatManager.message("point_blocks.ouch");
    private static final boolean async = plugin.getConfigPreferences().isAsync();
    final ArmorStand stand;
    private final Arena arena;
    private final Location availableLocation;
    private final double multiplier;
    private double y;
    private int waitedMs = ArenaOption.WAIT_MILLISECONDS.getDefault();
    private boolean forward = true, waitedAbove = true;
    private Listener listener;

    public PointBlock(Arena arena) {
        this.arena = arena;
        this.multiplier = plugin.getConfigPreferences().getPointBlockMultiplier();
        this.availableLocation = arena.getAvailableLocation();

        stand = (ArmorStand) availableLocation.getWorld().spawnEntity(availableLocation.clone().add(.5, -1.2, .5), EntityType.ARMOR_STAND);
        stand.setHelmet(plugin.getSkullManager().getPointBlock(arena, this.getPointBlockType()));
        stand.setCustomName(getCustomName());
        stand.setCustomNameVisible(true);
        stand.setGravity(false);
        stand.setVisible(false);

        Utils.rotateGameBlocks(stand, availableLocation, arena.getStartLocation(), PUNCH_ME.equals(stand.getCustomName()) ? "Punch-Me" : "Dont-Punch-Me");

        Utils.trySilently(
            () -> stand.setShieldBlockingDelay(1),
            () -> stand.setSilent(true),
            () -> stand.setPersistent(false)
        );

        arena.getPointBlocks().add(this);
        arena.getLocations().remove(availableLocation);

        registerEvents();
    }

    public void clear() {
        this.cancel();
        this.stand.remove();
        this.arena.getLocations().add(availableLocation);

        HandlerList.unregisterAll(listener);
    }

    public void handleItself() {
        if (async) {
            this.runTaskTimerAsynchronously(plugin, 1L, 1L);
        } else {
            this.runTaskTimer(plugin, 1L, 1L);
        }
    }

    private PointBlockType getPointBlockType() {
        int greenSize = (int) arena.getPointBlocks().stream().filter(pointBlock -> pointBlock.stand.getCustomName().equalsIgnoreCase(PUNCH_ME)).count(),
            redSize = (int) arena.getPointBlocks().stream().filter(pointBlock -> pointBlock.stand.getCustomName().equalsIgnoreCase(DONT_PUNCH_ME)).count();

        if (greenSize > redSize) {
            return PointBlockType.RED_BLOCK;
        } else if (greenSize == redSize) {
            return PointBlockType.GREEN_BLOCK;
        }

        return PointBlockType.GREEN_BLOCK;
    }

    private String getCustomName() {
        return stand.getHelmet().getItemMeta().getLore().contains("greenBlock") ? PUNCH_ME : DONT_PUNCH_ME;
    }

    private void registerEvents() {
        plugin.getServer().getPluginManager().registerEvents(listener = new Listener() {

            @EventHandler
            public void onArmorStandManipulate(PlayerArmorStandManipulateEvent event) {
                Player player = event.getPlayer();

                if (!arena.containPlayer(player)) return;

                ArmorStand armorStand = event.getRightClicked();

                if (!armorStand.equals(stand)) return;

                event.setCancelled(true);
            }

            @EventHandler
            public void onArmorStandDamage(EntityDamageByEntityEvent event) {
                if (!(event.getDamager() instanceof Player)) return;
                if (!(event.getEntity() instanceof ArmorStand)) return;

                Player player = (Player) event.getDamager();
                ArmorStand armorStand = (ArmorStand) event.getEntity();

                if (!arena.containPlayer(player)) return;
                if (!armorStand.equals(stand)) return;

                User user = plugin.getUserManager().getUser(player);
                String name = stand.getCustomName();

                if (name == null) return;
                if (stand.getCustomName().equals(OUCH)) return;

                if (name.equalsIgnoreCase(PUNCH_ME)) {
                    user.addStat(LOCAL_SCORE, 1);
                    user.addStat(LOCAL_STREAK, 1);
                    user.addStat(PLUS_BLOCKS, 1);

                    int localStreak = user.getStat(LOCAL_STREAK);

                    if (localStreak > user.getStat(LOCAL_LONGEST_STREAK)) {
                        user.setStat(LOCAL_LONGEST_STREAK, localStreak);
                    }

                    plugin.getSoundManager().playSound(player, SoundManager.GameSound.POINT_SOUND);
                    plugin.getRewardsFactory().performReward(arena, Reward.RewardType.SUCCESSFUL_POINT);
                } else if (name.equalsIgnoreCase(DONT_PUNCH_ME)) {
                    user.addStat(LOCAL_SCORE, -1);
                    user.addStat(MINUS_BLOCKS, 1);
                    user.setStat(LOCAL_STREAK, 0);

                    plugin.getSoundManager().playSound(player, SoundManager.GameSound.MINUS_POINT_SOUND);
                    plugin.getRewardsFactory().performReward(arena, Reward.RewardType.WRONG_POINT);
                }

                event.setCancelled(true);

                stand.setHelmet(plugin.getSkullManager().getPointBlock(arena, PointBlockType.CYAN_BLOCK));
                stand.setCustomName(OUCH);

                Utils.rotateGameBlocks(stand, availableLocation, arena.getStartLocation(), "Ouch");
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

                arena.getPointBlocks().remove(this);
                arena.getLocations().add(availableLocation);

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
            if (XReflection.supports(16)) {
                stand.teleportAsync(destination);
            } else {
                plugin.getServer().getScheduler().runTask(plugin, () -> stand.teleport(destination));
            }
        } else {
            stand.teleport(destination);
        }
    }
}
