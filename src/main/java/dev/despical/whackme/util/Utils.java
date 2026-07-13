package dev.despical.whackme.util;

import dev.despical.commons.number.NumberUtils;
import dev.despical.commons.serializer.InventorySerializer;
import dev.despical.whackme.WhackMe;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * @author Despical
 * <p>
 * Created at 20.06.2022
 */
@UtilityClass
public final class Utils {

    private static final WhackMe PLUGIN = WhackMe.getInstance();
    public static final int[][] DIRECTIONS = {{1, 0}, {-1, 0}, {1, 1}, {-1, 1}, {1, -1}, {-1, -1}, {0, 1}, {0, -1}};

    public static boolean isSurroundedBy(Location center) {
        if (center == null) return false;

        center = new Location(center.getWorld(), center.getX(), center.getY(), center.getZ());

        for (Location location : getLocationsSurroundedBy(center)) {
            if (location.getBlock().getType() != Material.END_PORTAL_FRAME) return false;
        }

        return true;
    }

    public static List<Location> getLocationsSurroundedBy(Location center) {
        List<Location> locations = new ArrayList<>();

        for (int[] array : DIRECTIONS) {
            Location location = center.clone().add(array[0], 0, array[1]);
            locations.add(location.getBlock().getLocation());
        }

        return locations;
    }

    public static void rotateGameBlocks(ArmorStand stand, Location availableLocation, Location center, String path) {
        Bukkit.getScheduler().runTaskLater(PLUGIN, () -> {
            String[] yaws = PLUGIN.getConfig().getString("point-blocks.rotations." + path.toLowerCase(Locale.ROOT), "").split(":");

            Location newCenter = new Location(center.getWorld(), center.getX(), center.getY(), center.getZ());
            List<Location> locations = getLocationsSurroundedBy(newCenter);
            int i = -1;

            for (Location location : locations) {
                i++;

                if (location.equals(availableLocation)) {
                    break;
                }
            }

            if (i >= yaws.length) return;
            int yaw = NumberUtils.getInt(yaws[i]);

            Location location = stand.getLocation().clone();
            location.setYaw(yaw);

            stand.teleport(location);
        }, 1);
    }

    public static String format(String string, Var... variables) {
        for (Var variable : variables) {
            string = string.replace(variable.name, variable.value.toString());
        }

        return string;
    }

    public static void resetPlayerAttributes(Player player) {
        player.setHealth(20D);
        player.setFoodLevel(20);
        player.setExp(0F);
        player.setExhaustion(0F);
        player.setSaturation(20F);
        player.setFireTicks(0);
        player.setFlying(false);
        player.setItemOnCursor(null);
        player.setAllowFlight(false);
        player.setFlying(false);
        player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
        player.setGameMode(GameMode.ADVENTURE);
    }

    public static void restoreSavedPlayerState(Player player) {
        player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
        InventorySerializer.loadInventory(PLUGIN, player);
    }
}
