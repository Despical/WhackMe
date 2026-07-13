package dev.despical.whackme.command;

import dev.despical.commandframework.CommandArguments;
import dev.despical.commandframework.annotations.Command;
import dev.despical.whackme.api.event.player.PlayerLeaveGameEvent;
import dev.despical.whackme.arena.Arena;
import dev.despical.whackme.game.StopReason;
import dev.despical.whackme.option.BooleanOption;
import dev.despical.whackme.user.User;
import dev.despical.whackme.util.Var;
import org.bukkit.entity.Player;

/**
 * @author Despical
 * <p>
 * Created at 6.12.2025
 */
public final class AdminCommands extends CommandCategory {

    @Command(
        name = "whackme",
        aliases = "wm",
        fallbackPrefix = "thewhackme",
        permission = "whackme.command.help",
        usage = "/%label% help",
        desc = "Main command of the Whack Me."
    )
    public void mainCommand(CommandArguments arguments) {
        if (arguments.isArgumentsEmpty()) {
            arguments.sendMessage("&3This server is running &bWhackMe v{0} &3by &bDespical&3.", plugin.getDescription().getVersion());

            if (arguments.hasPermission("whackme.admin")) {
                arguments.sendMessage("&3Commands: &b/{0} help", arguments.getLabel());
            }

            return;
        }

        chatManager.sendMessage(arguments, "unrecognized-arguments", Var.of("%label%", arguments.getLabel()), Var.of("%arguments%", arguments.concatArguments()));
    }

    @Command(
        name = "whackme",
        aliases = "wm.reload",
        permission = "whackme.admin.reload",
        usage = "/%label% reload",
        desc = "Reloads configuration files."
    )
    public void reloadCommand(CommandArguments arguments) {
        chatManager.loadFile();
        plugin.getOptions().reloadOptions();
        plugin.getPlayingCommandPolicy().reload();
        plugin.registerItems();
        plugin.getEventManager().reload();
        plugin.getSignManager().reload();
        plugin.getBossBarConfig().reload();
        plugin.getSoundManager().reload();
        plugin.getGameManager().reload();

        chatManager.sendMessage(arguments, "reloaded-configuration");
    }

    @Command(
        name = "whackme.stop",
        aliases = "wm.stop",
        permission = "whackme.admin.stop",
        usage = "/%label% stop [arena]",
        desc = "Stops the current or specified arena game.",
        max = 1
    )
    public void stopCommand(CommandArguments arguments) {
        boolean isConsoleSender = arguments.isSenderConsole();

        if (arguments.isArgumentsEmpty()) {
            if (isConsoleSender) {
                chatManager.sendMessage(arguments, "stop-command.correct-usage", Var.of("%label%", arguments.getLabel()));
                return;
            }

            Player player = arguments.getSender();
            Arena arena = arenaRegistry.getArena(player);

            if (arena == null) {
                chatManager.sendMessage(player, "not-playing");
                return;
            }

            arenaManager.stopArena(arena, StopReason.STOP_COMMAND);
            return;
        }

        Arena arena = arenaRegistry.getArena(arguments.getFirst());

        if (arena == null) {
            chatManager.sendMessage(arguments, "no-arena-found-with-that-name");
            return;
        }

        if (arena.getPlayer() == null) {
            chatManager.sendMessage(arguments, "stop-command.not-playing");
            return;
        }


        arenaManager.stopArena(arena, StopReason.STOP_COMMAND);

        if (!isConsoleSender && arena.isPlaying(arguments.<Player>getSender())) {
            return;
        }

        chatManager.sendMessage(arguments, "stop-command.stopped");
    }

    @Command(
        name = "whackme.help",
        aliases = "wm.help",
        permission = "whackme.command.help",
        usage = "/%label% help"
    )
    public void helpCommand(User user, CommandArguments arguments) {
        Var var = Var.of("%label%", arguments.getLabel());
        chatManager.sendMessage(arguments, "help-message", var);

        if (arguments.hasPermission("whackme.admin.help")) {
            arguments.sendMessage("");
            chatManager.sendMessage(arguments, "admin-help-message", var);
            arguments.sendMessage("");

            if (BooleanOption.DEBUG.value()) {
                chatManager.sendMessage(arguments, "debug-help-message", var);
            }
        }
    }

    @Command(
        name = "whackme.kick",
        aliases = "wm.kick",
        permission = "whackme.admin.kick",
        usage = "/%label% kick <player>",
        desc = "Removes a player from their active Whack Me game and teleports them to the arena's end location.",
        min = 1,
        max = 1,
        senderType = Command.SenderType.PLAYER
    )
    public void kickCommand(User user, CommandArguments arguments) {
        Player targetPlayer = arguments.getPlayer(0).orElseGet(() -> {
            chatManager.sendMessage(arguments, "no-player-with-that-name");
            return null;
        });

        if (targetPlayer == null) {
            return;
        }

        User targetUser = plugin.getUserManager().getUser(targetPlayer);
        Arena playerArena = targetUser.getArena();

        if (playerArena == null) {
            chatManager.sendMessage(arguments, "kick-command.not-playing", Var.ofPlayer(targetPlayer));
            return;
        }

        arenaManager.leaveAttempt(targetUser, PlayerLeaveGameEvent.LeaveReason.KICK);

        chatManager.sendMessage(arguments, "kick-command.kicked",
            Var.of("%player%", targetPlayer.getName()),
            Var.of("%arena%", playerArena.getId()));
    }
}
