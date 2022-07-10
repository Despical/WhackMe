package me.despical.whackme.util;

import me.despical.commons.compat.XMaterial;
import me.despical.commons.item.ItemBuilder;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.function.Consumer;

/**
 * @author Despical
 * <p>
 * Created at 20.06.2022
 */
public class Utils {

	public final static ItemStack
		GREEN_TERRACOTTA = new ItemBuilder(XMaterial.GREEN_TERRACOTTA).build(),
		RED_TERRACOTTA = new ItemBuilder(XMaterial.RED_TERRACOTTA).build(),
		CYAN_TERRACOTTA = new ItemBuilder(XMaterial.CYAN_TERRACOTTA).build();

	public static boolean isSurroundedBy(Location center) {
		if (center == null) return false;

		Material endPortalFrame = XMaterial.END_PORTAL_FRAME.parseMaterial();

		for (Block block : Objects.requireNonNull(getBlocksSurroundedBy(center))) {
			if (block.getType() != endPortalFrame) return false;
		}

		return true;
	}

	public static Set<Block> getBlocksSurroundedBy(Location center) {
		final int[][] arrays = {{1, 0}, {-1, 0}, {1, 1}, {-1, 1}, {1, -1}, {-1, -1}, {0, 1}, {0, -1}};
		final Set<Block> blocks = new HashSet<>();

		for (int[] array : arrays) {
			Block block = center.clone().add(array[0], 0, array[1]).getBlock();

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
}