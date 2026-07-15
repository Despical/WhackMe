package dev.despical.whackme.arena.blocks;

import java.util.Collection;

/**
 * @author Despical
 * <p>
 * Created at 14.07.2026
 */
final class PointBlockTypeSelector {

    PointBlockType select(Collection<PointBlock> activeBlocks) {
        long greenBlocks = activeBlocks.stream()
            .filter(block -> block.getType() == PointBlockType.GREEN_BLOCK)
            .count();
        long redBlocks = activeBlocks.stream()
            .filter(block -> block.getType() == PointBlockType.RED_BLOCK)
            .count();

        return greenBlocks > redBlocks ? PointBlockType.RED_BLOCK : PointBlockType.GREEN_BLOCK;
    }
}
