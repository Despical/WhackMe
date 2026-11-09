package dev.despical.whackme.arena.blocks;

import java.util.Collection;

/**
 * @author Despical
 * <p>
 * Created at 14.07.2026
 */
final class PointBlockTypeSelector {

    PointBlockType select(Collection<PointBlock> activeBlocks) {
        int greenBlocks = 0;
        int redBlocks = 0;

        // One pass counts both colors.
        for (PointBlock block : activeBlocks) {
            PointBlockType type = block.getType();

            if (type == PointBlockType.GREEN_BLOCK) {
                greenBlocks++;
            } else if (type == PointBlockType.RED_BLOCK) {
                redBlocks++;
            }
        }

        return greenBlocks > redBlocks ? PointBlockType.RED_BLOCK : PointBlockType.GREEN_BLOCK;
    }
}
