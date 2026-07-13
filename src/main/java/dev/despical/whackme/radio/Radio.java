package dev.despical.whackme.radio;

import dev.despical.whackme.arena.Arena;
import org.bukkit.entity.Player;

/**
 * @author Despical
 * <p>
 * Created at 13.07.2026
 */
public interface Radio {

    void addArena(Arena arena);

    void removeArena(Arena arena);

    void addPlayer(Arena arena, Player player);

    void removePlayer(Arena arena, Player player);
}
