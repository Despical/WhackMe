package me.despical.whackme.handlers.sign;

import me.despical.whackme.arena.Arena;
import org.bukkit.block.Sign;

/**
 * @author Despical
 * <p>
 * Created at 31.01.2024
 */
public class ArenaSign {

    private final Sign sign;
    private final Arena arena;

    public ArenaSign(Sign sign, Arena arena) {
        this.sign = sign;
        this.arena = arena;
    }

    public Sign getSign() {
        return (Sign) sign.getBlock().getState();
    }

    public Arena getArena() {
        return arena;
    }
}