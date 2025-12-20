package dev.despical.whackme.command;

import dev.despical.commandframework.CommandArguments;
import dev.despical.commandframework.CommandFramework;
import dev.despical.whackme.WhackMe;
import dev.despical.whackme.handler.ChatManager;
import org.bukkit.entity.Player;

public abstract class CommandHandler {

    protected static final WhackMe plugin = WhackMe.getInstance();
    protected static final ChatManager chatManager = plugin.getChatManager();

    static {
        CommandFramework commandFramework = plugin.getCommandFramework();
        commandFramework.addCustomParameter("Player", CommandArguments::<Player>getSender);
        commandFramework.addCustomParameter("Arena", arguments -> plugin.getArenaRegistry().getArena(arguments.getArgument(0)));
        commandFramework.addCustomParameter("pArena", arguments -> plugin.getArenaRegistry().getArena(arguments.<Player>getSender()));
    }

    public CommandHandler() {
        plugin.getCommandFramework().registerCommands(this);
    }
}
