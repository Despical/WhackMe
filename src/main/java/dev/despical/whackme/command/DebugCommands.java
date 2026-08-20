package dev.despical.whackme.command;

import dev.despical.commandframework.annotations.Command;
import dev.despical.commandframework.annotations.Option;
import dev.despical.commandframework.debug.Debug;
import net.kyori.adventure.text.Component;

import java.util.List;

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
        usage = "/%label% debug component [message] [--path=message.path]",
        senderType = Command.SenderType.PLAYER
    )
    @Option("path")
    public void debugComponentCommand(Arguments arguments) {
        List<String> pathOption = arguments.getOption("path");
        String path = pathOption == null || pathOption.isEmpty() ? null : pathOption.getFirst();

        if (path != null && !path.isBlank()) {
            List<String> messages = chatManager.getStringList(path);
            if (!messages.isEmpty()) {
                messages.stream()
                    .map(chatManager::parseMessage)
                    .forEach(arguments::sendMessage);
                return;
            }

            arguments.sendConfiguredMessage(path);
            return;
        }

        if (arguments.isArgumentsEmpty()) {
            arguments.sendRawMessage("<#FF5252>✖ <#BDBDBD>Provide a MiniMessage string or use <#FFCA28>--path=<message.path><#BDBDBD>.");
            return;
        }

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
