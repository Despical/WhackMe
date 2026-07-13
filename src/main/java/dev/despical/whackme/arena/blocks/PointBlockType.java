package dev.despical.whackme.arena.blocks;

import dev.despical.whackme.arena.options.ArenaKeys;
import dev.despical.whackme.arena.options.ArenaOption;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.inventory.ItemStack;

/**
 * @author Despical
 * <p>
 * Created at 11.10.2024
 */
@Getter
@RequiredArgsConstructor
public enum PointBlockType {

    GREEN_BLOCK("punch-me", "Green Block"),
    RED_BLOCK("dont-punch-me", "Red Block"),
    GRAY_BLOCK("ouch", "Gray Block");

    private final String path;
    private final String displayName;

    public ArenaOption<ItemStack> getArenaOption() {
        return switch (this) {
            case GREEN_BLOCK -> ArenaKeys.GREEN_BLOCK_ITEM;
            case RED_BLOCK -> ArenaKeys.RED_BLOCK_ITEM;
            case GRAY_BLOCK -> ArenaKeys.GRAY_BLOCK_ITEM;
        };
    }
}
