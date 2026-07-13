package dev.despical.whackme.util;

import dev.despical.whackme.WhackMe;
import dev.despical.whackme.user.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.UUID;

/**
 * @author Despical
 * <p>
 * Created at 01.06.2026
 */
public class Var {

    private static final WhackMe PLUGIN = WhackMe.getInstance();
    private static final String NONE = PLUGIN.getChatManager().getRawString("none");
    private static final Var EMPTY_PLAYER = new Var("%player%", "null");

    public final String name;
    public final Object value;

    private Var(String name, Object value) {
        this.name = name;
        this.value = value;
    }

    public static Var of(String name, Object value) {
        return new Var(name, value);
    }

    public static Var of(String name, Collection<String> list) {
        return new Var(name, list.isEmpty() ? NONE : String.join(", ", list));
    }

    public static Var ofPlayer(Player player) {
        if (player == null) {
            return EMPTY_PLAYER;
        }

        return new Var("%player%", player.getDisplayName());
    }

    public static Var ofPlayer(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);

        if (player == null) {
            return EMPTY_PLAYER;
        }

        return ofPlayer(player);
    }

    public static Var ofPlayer(User user) {
        return new Var("%player%", user.getName());
    }
}
