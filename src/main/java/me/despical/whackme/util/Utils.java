package me.despical.whackme.util;

import me.despical.commons.compat.XMaterial;
import me.despical.commons.item.ItemBuilder;
import me.despical.whackme.Main;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.function.Consumer;

/**
 * @author Despical
 * <p>
 * Created at 20.06.2022
 */
public class Utils {

	private static final Main plugin = JavaPlugin.getPlugin(Main.class);

	public static final ItemStack
		GREEN_TERRACOTTA = new ItemBuilder(XMaterial.GREEN_TERRACOTTA).build(),
		RED_TERRACOTTA = new ItemBuilder(XMaterial.RED_TERRACOTTA).build(),
		CYAN_TERRACOTTA = new ItemBuilder(XMaterial.CYAN_TERRACOTTA).build(),
		END_PORTAL_FRAME = new ItemBuilder(XMaterial.END_PORTAL_FRAME).build();

	private static final int[][] directions = {{1, 0}, {-1, 0}, {1, 1}, {-1, 1}, {1, -1}, {-1, -1}, {0, 1}, {0, -1}};

	public static boolean isSurroundedBy(Location center) {
		if (center == null) return false;

		for (Block block : Objects.requireNonNull(getBlocksSurroundedBy(center))) {
			if (block.getType() != END_PORTAL_FRAME.getType()) return false;
		}

		return true;
	}

	public static Set<Block> getBlocksSurroundedBy(Location center) {
		final Set<Block> blocks = new HashSet<>();

		for (int[] array : directions) {
			final Block block = center.clone().add(array[0], 0, array[1]).getBlock();

			blocks.add(block);
		}

		return blocks;
	}

	public static void trySilently(Consumer<?> consumer) {
		try {
			consumer.accept(null);
		} catch (Exception ignored) {
		}
	}

	public static boolean hasJoinPermission(Player player) {
		final String permission = plugin.getConfig().getString("Join-Permission");

		return permission != null && (permission.isEmpty() || (player != null && player.hasPermission(permission)));
	}
}