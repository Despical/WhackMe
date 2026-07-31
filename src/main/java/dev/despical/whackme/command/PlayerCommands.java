package dev.despical.whackme.command;

import dev.despical.commandframework.annotations.Command;
import dev.despical.whackme.api.event.player.PlayerLeaveGameEvent;
import dev.despical.whackme.arena.Arena;
import dev.despical.whackme.menu.stats.StatsMenu;
import dev.despical.whackme.user.User;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

/**
 * @author Despical
 * <p>
 * Created at 6.12.2025
 */
public final class PlayerCommands extends CommandCategory {

    @Command(
        name = "whackme.join",
        aliases = "wm.join",
        usage = "/%label% join <arena>",
        min = 1,
        senderType = Command.SenderType.PLAYER
    )
    public void joinCommand(User user, Arguments arguments) {
        String arenaId = arguments.getFirst();
        Arena arena = arenaRegistry.getArena(arenaId);

        if (arena == null) {
            arguments.sendMessage("no-arena-found-with-that-name");
            return;
        }

        arenaManager.joinAttempt(user, arena);
    }

    @Command(
        name = "whackme.leave",
        aliases = "wm.leave",
        usage = "/%label% leave",
        senderType = Command.SenderType.PLAYER
    )
    public void leaveCommand(User user) {
        arenaManager.leaveAttempt(user, PlayerLeaveGameEvent.LeaveReason.LEAVE_COMMAND);
    }

    @Command(
        name = "whackme.stats",
        aliases = "wm.stats",
        usage = "/%label% stats [player]",
        senderType = Command.SenderType.PLAYER
    )
    public void statsCommand(User user, Arguments arguments) {
        if (arguments.isArgumentsEmpty()) {
            new StatsMenu(user).open();
            return;
        }

        Player target = arguments.getPlayer(0).orElse(null);

        if (target != null) {
            new StatsMenu(user, target).open();
            return;
        }

        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayerIfCached(arguments.getFirst());

        if (offlinePlayer == null) {
            arguments.sendMessage("no-player-with-that-name");
            return;
        }

        new StatsMenu(user, offlinePlayer).open();
    }
}
