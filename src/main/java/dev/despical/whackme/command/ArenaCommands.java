package dev.despical.whackme.command;

import dev.despical.commandframework.CommandArguments;
import dev.despical.commandframework.annotations.Command;
import dev.despical.commandframework.annotations.Flag;
import dev.despical.whackme.arena.Arena;
import dev.despical.whackme.arena.options.ArenaKeys;
import dev.despical.whackme.game.StopReason;
import dev.despical.whackme.setup.SetupMenu;
import dev.despical.whackme.user.User;
import dev.despical.whackme.util.Var;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.StringJoiner;

/**
 * @author Despical
 * <p>
 * Created at 6.12.2025
 */
public final class ArenaCommands extends CommandCategory {

    @Command(
        name = "whackme.create",
        aliases = "wm.create",
        permission = "whackme.arena.create",
        usage = "/%label% create <arena id>",
        min = 1,
        max = 1
    )
    public void createArenaCommand(CommandArguments arguments) {
        Player player = arguments.getSender();
        String arenaId = arguments.getFirst();
        Var var = Var.of("%id%", arenaId);

        if (arenaRegistry.isArenaExists(arenaId)) {
            chatManager.sendCenteredMessage(player, "arena-already-exists", var);

            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            return;
        }

        arenaRegistry.registerNewArena(arenaId);
        chatManager.sendCenteredMessage(player, "created-arena", var);

        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 2.0f);
    }

    @Flag({"confirm", "cancel"})
    @Command(
        name = "whackme.delete",
        aliases = "wm.delete",
        permission = "whackme.arena.delete",
        usage = "/%label% delete <arena id> [--confirm] [--cancel]",
        min = 1,
        senderType = Command.SenderType.PLAYER
    )
    public void deleteArenaCommand(Arena arena, CommandArguments arguments) {
        Player player = arguments.getSender();
        Var var = Var.of("%id%", arguments.getFirst());

        if (arena == null) {
            chatManager.sendCenteredMessage(player, "no-arena-found", var);
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            return;
        }

        if (arguments.isFlagPresent("cancel")) {
            chatManager.sendCenteredMessage(player, "delete-cancelled", var);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            return;
        }

        if (arguments.isFlagPresent("confirm")) {
            if (arena.getPlayer() != null) {
                arenaManager.stopArena(arena, StopReason.ARENA_DELETED);
            }

            arenaRegistry.unregisterArena(arena);
            chatManager.sendCenteredMessage(player, "deleted-arena", var);

            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1f, 0.5f);
            return;
        }

        chatManager.sendCenteredMessage(player, "delete-confirmation", var);
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1.5f);
    }

    @Command(
        name = "whackme.list",
        aliases = "wm.list",
        permission = "whackme.arena.list",
        usage = "/%label% list",
        senderType = Command.SenderType.PLAYER
    )
    public void listArenaCommand(User user, CommandArguments arguments) {
        Player player = arguments.getSender();
        Set<Arena> arenas = arenaRegistry.getArenas();

        if (arenas.isEmpty()) {
            chatManager.sendMessage(player, "no-arenas-registered");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            return;
        }

        StringJoiner arenasJoiner = new StringJoiner("<dark_gray>, ");

        for (Arena arena : arenas) {
            boolean isReady = arena.getOption(ArenaKeys.READY);

            if (isReady) {
                arenasJoiner.add(
                    "<#00E676><hover:show_text:'<#00E676><b>✔ Ready to play!</b><br><gray>Click to join.'><click:run_command:'/wm join %s'>%1$s</click></hover>".formatted(arena.getId())
                );
            } else {
                arenasJoiner.add(
                    "<#FF5252><hover:show_text:'<#FF5252><b>✖ Setup Incomplete!</b><br><gray>Click to edit.'><click:run_command:'/wm edit %s'>%1$s</click></hover>".formatted(arena.getId())
                );
            }
        }

        chatManager.sendMessage(player, "created-arenas", Var.of("%arenas%", arenasJoiner.toString()));
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
    }

    @Command(
        name = "whackme.edit",
        aliases = "wm.edit",
        permission = "whackme.arena.edit",
        usage = "/%label% edit <arena id>",
        min = 1,
        senderType = Command.SenderType.PLAYER
    )
    public void editArenaCommand(Arena arena, CommandArguments arguments) {
        if (arena == null) {
            chatManager.sendMessage(arguments, "no-arena-found-with-that-name");
            return;
        }

        new SetupMenu(plugin, arena, arguments.getSender());
    }
}
