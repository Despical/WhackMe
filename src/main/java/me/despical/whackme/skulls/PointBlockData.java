package me.despical.whackme.skulls;

import me.despical.whackme.arena.blocks.PointBlockType;
import org.bukkit.inventory.ItemStack;

import java.util.EnumMap;
import java.util.Map;

/**
 * @author Despical
 * <p>
 * Created at 11.10.2024
 */
public class PointBlockData {

	private final Map<PointBlockType, ItemStack> pointBlocks;

	PointBlockData() {
		this.pointBlocks = new EnumMap<>(PointBlockType.class);
	}

	void addPointBlock(PointBlockType type, ItemStack item) {
		this.pointBlocks.put(type, item);
	}

	public ItemStack getPointBlock(PointBlockType type) {
		return this.pointBlocks.get(type);
	}
}