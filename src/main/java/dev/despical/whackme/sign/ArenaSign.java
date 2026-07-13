package dev.despical.whackme.sign;

import dev.despical.whackme.arena.Arena;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

/**
 * @author Despical
 * <p>
 * Created at 12.12.2025
 */
public record ArenaSign(Arena arena, Block block) {

    public Sign sign() {
        return (Sign) block.getState();
    }
}
