package me.despical.whackme.utils;

import me.despical.commons.compat.XMaterial;
import me.despical.commons.item.ItemBuilder;
import me.despical.commons.number.NumberUtils;
import me.despical.whackme.WhackMe;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

/**
 * @author Despical
 * <p>
 * Created at 20.06.2022
 */
public class Utils {

	private static final WhackMe plugin = JavaPlugin.getPlugin(WhackMe.class);

	public static final ItemStack END_PORTAL_FRAME = new ItemBuilder(XMaterial.END_PORTAL_FRAME).build();
	public static final int[][] DIRECTIONS = {{1, 0}, {-1, 0}, {1, 1}, {-1, 1}, {1, -1}, {-1, -1}, {0, 1}, {0, -1}};

	public static boolean isSurroundedBy(Location center) {
		if (center == null) return false;

		center = new Location(center.getWorld(), center.getX(), center.getY(), center.getZ());

		for (final Block block : getBlocksSurroundedBy(center)) {
			if (block.getType() != END_PORTAL_FRAME.getType()) return false;
		}

		return true;
	}

	public static Set<Block> getBlocksSurroundedBy(Location center) {
		final Set<Block> blocks = new HashSet<>();

		for (int[] array : DIRECTIONS) {
			final Block block = center.clone().add(array[0], 0, array[1]).getBlock();

			blocks.add(block);
		}

		return blocks;
	}

	public static void trySilently(Runnable... runnables) {
		for (Runnable runnable : runnables) {
			try {
				runnable.run();
			} catch (Exception | Error ignored) {
			}
		}
	}

	public static boolean hasJoinPermission(Player player) {
		final String permission = plugin.getConfig().getString("Join-Permission");

		return permission == null || permission.isEmpty() || player != null && player.hasPermission(permission);
	}

	public static void rotateGameBlocks(ArmorStand stand, Location availableLocation, Location center, String path) {
		plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
			String[] yaws = plugin.getConfig().getString("Point-Blocks.Rotations." + path, "").split(":");

			Location newCenter = new Location(center.getWorld(), center.getX(), center.getY(), center.getZ());
			Set<Block> locations = getBlocksSurroundedBy(newCenter);
			int i = -1;

			for (Block block : locations) {
				i++;

				if (block.getLocation().equals(availableLocation)) {
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
}