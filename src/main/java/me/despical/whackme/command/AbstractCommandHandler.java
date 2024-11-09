package me.despical.whackme.command;

import me.despical.commandframework.CommandArguments;
import me.despical.commandframework.CommandFramework;
import me.despical.commandframework.Message;
import me.despical.commons.util.Strings;
import me.despical.whackme.WhackMe;
import me.despical.whackme.handlers.ChatManager;
import org.bukkit.entity.Player;

import java.util.stream.Stream;

public abstract class AbstractCommandHandler {

    protected static final WhackMe plugin = WhackMe.getInstance();
    protected static final ChatManager chatManager = plugin.getChatManager();

    static {
        CommandFramework commandFramework = plugin.getCommandFramework();
        commandFramework.addCustomParameter("Player", CommandArguments::<Player>getSender);
        commandFramework.addCustomParameter("Arena", arguments -> plugin.getArenaRegistry().getArena(arguments.getArgument(0)));
        commandFramework.addCustomParameter("pArena", arguments -> plugin.getArenaRegistry().getArena(arguments.<Player>getSender()));

        Message.setColorFormatter(Strings::format);

        Stream.of(Message.SHORT_ARG_SIZE, Message.LONG_ARG_SIZE).forEach(message -> message.setMessage((command, arguments) -> {
            arguments.sendMessage(chatManager.prefixedMessage("commands.correct_usage").replace("%usage%", command.usage()));
            return true;
        }));
    }

    public AbstractCommandHandler() {
        plugin.getCommandFramework().registerCommands(this);
    }
}
