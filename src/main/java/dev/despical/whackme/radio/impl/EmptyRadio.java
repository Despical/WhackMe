package dev.despical.whackme.radio.impl;

import dev.despical.whackme.arena.Arena;
import dev.despical.whackme.radio.Radio;
import org.bukkit.entity.Player;

/**
 * @author Despical
 * <p>
 * Created at 13.07.2026
 */
public class EmptyRadio implements Radio {

    @Override
    public void addArena(Arena arena) {
    }

    @Override
    public void removeArena(Arena arena) {
    }

    @Override
    public void addPlayer(Arena arena, Player player) {
    }

    @Override
    public void removePlayer(Arena arena, Player player) {
    }
}
