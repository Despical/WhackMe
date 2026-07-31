package dev.despical.whackme.command;

import dev.despical.commandframework.CommandArguments;
import dev.despical.commandframework.annotations.Command;
import dev.despical.commandframework.debug.Debug;
import net.kyori.adventure.text.Component;

/**
 * @author Despical
 * <p>
 * Created at 2.6.2026
 */
@Debug
public final class DebugCommands extends CommandCategory {

    @Command(
        name = "whackme.debug.component",
        aliases = "wm.debug.component",
        permission = "whackme.debug.component",
        usage = "/%label% debug component <message>",
        min = 1,
        senderType = Command.SenderType.PLAYER
    )
    public void debugComponentCommand(Arguments arguments) {
        Component component = chatManager.parseMessage(arguments.concatArguments());
        arguments.sendMessage(component);
    }

    @Command(
        name = "whackme.debug.dump",
        aliases = "wm.debug.dump",
        permission = "whackme.debug.dump",
        usage = "/%label% debug dump"
    )
    public void debugDumpTimingsCommand(Arguments arguments) {
        plugin.getEventManager().sendTimingsReport(arguments.getSender());
    }
}
