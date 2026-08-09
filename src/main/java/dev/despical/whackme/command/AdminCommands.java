package dev.despical.whackme.command;

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
        usage = "/whackme help",
        desc = "Main command of the Whack Me."
    )
    public void mainCommand(Arguments arguments) {
        if (arguments.isArgumentsEmpty()) {
            arguments.sendRawMessage("<#00aaaa>This server is running <#55ffff>Whack Me v%version% <#00aaaa>by <#55ffff>Despical<#00aaaa>.",
                Var.of("%version%", plugin.getDescription().getVersion()));

            if (arguments.hasPermission("whackme.admin.help")) {
                arguments.sendRawMessage("<#00aaaa>Commands: <#55ffff>/%label% help",
                    Var.of("%label%", arguments.getLabel()));
            }

            return;
        }

        arguments.sendConfiguredMessage("unrecognized-arguments", Var.of("%label%", arguments.getLabel()), Var.of("%arguments%", arguments.concatArguments()));
    }

    @Command(
        name = "whackme.reload",
        aliases = "wm.reload",
        permission = "whackme.admin.reload",
        usage = "/%label% reload",
        desc = "Reloads configuration files."
    )
    public void reloadCommand(Arguments arguments) {
        chatManager.loadFile();
        plugin.getOptions().reloadOptions();
        plugin.getPlayingCommandPolicy().reload();
        plugin.registerItems();
        plugin.getEventManager().reload();
        plugin.getSignManager().reload();
        plugin.getBossBarConfig().reload();
        plugin.getSoundManager().reload();
        plugin.getGameManager().reload();

        arguments.sendConfiguredMessage("reloaded-configuration");
    }

    @Command(
        name = "whackme.stop",
        aliases = "wm.stop",
        permission = "whackme.admin.stop",
        usage = "/%label% stop [arena]",
        desc = "Stops the current or specified arena game.",
        max = 1
    )
    public void stopCommand(Arguments arguments) {
        boolean isConsoleSender = arguments.isSenderConsole();

        if (arguments.isArgumentsEmpty()) {
            if (isConsoleSender) {
                arguments.sendConfiguredMessage("stop-command.correct-usage", Var.of("%label%", arguments.getLabel()));
                return;
            }

            Player player = arguments.getSender();
            Arena arena = arenaRegistry.getArena(player);

            if (arena == null) {
                arguments.sendConfiguredMessage("not-playing");
                return;
            }

            arenaManager.stopArena(arena, StopReason.STOP_COMMAND);
            return;
        }

        Arena arena = arenaRegistry.getArena(arguments.getFirst());

        if (arena == null) {
            arguments.sendConfiguredMessage("no-arena-found-with-that-name");
            return;
        }

        if (arena.getPlayer() == null) {
            arguments.sendConfiguredMessage("stop-command.not-playing");
            return;
        }

        arenaManager.stopArena(arena, StopReason.STOP_COMMAND);

        if (!isConsoleSender && arena.isPlaying(arguments.<Player>getSender())) {
            return;
        }

        arguments.sendConfiguredMessage("stop-command.stopped");
    }

    @Command(
        name = "whackme.help",
        aliases = "wm.help",
        permission = "whackme.command.help",
        usage = "/%label% help"
    )
    public void helpCommand(Arguments arguments) {
        Var var = Var.of("%label%", arguments.getLabel());
        arguments.sendConfiguredMessage("help-message", var);

        if (arguments.hasPermission("whackme.admin.help")) {
            arguments.sendBlankMessage();
            arguments.sendConfiguredMessage("admin-help-message", var);
            arguments.sendBlankMessage();

            if (BooleanOption.DEBUG.value()) {
                arguments.sendConfiguredMessage("debug-help-message", var);
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
        max = 1
    )
    public void kickCommand(Arguments arguments) {
        Player targetPlayer = arguments.getPlayer(0).orElseGet(() -> {
            arguments.sendConfiguredMessage("no-player-with-that-name");
            return null;
        });

        if (targetPlayer == null) {
            return;
        }

        User targetUser = plugin.getUserManager().getUser(targetPlayer);
        Arena playerArena = targetUser.getArena();

        if (playerArena == null) {
            arguments.sendConfiguredMessage("kick-command.not-playing", Var.ofPlayer(targetPlayer));
            return;
        }

        arenaManager.leaveAttempt(targetUser, PlayerLeaveGameEvent.LeaveReason.KICK);

        arguments.sendConfiguredMessage("kick-command.kicked",
            Var.of("%player%", targetPlayer.getName()),
            Var.of("%arena%", playerArena.getId())
        );
    }
}
