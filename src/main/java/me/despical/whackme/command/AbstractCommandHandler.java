package me.despical.whackme.command;

import me.despical.commandframework.CommandArguments;
import me.despical.commandframework.CommandFramework;
import me.despical.whackme.WhackMe;
import me.despical.whackme.handlers.ChatManager;
import org.bukkit.entity.Player;

public abstract class AbstractCommandHandler {

    protected static final WhackMe plugin = WhackMe.getInstance();
    protected static final ChatManager chatManager = plugin.getChatManager();

    static {
        CommandFramework commandFramework = plugin.getCommandFramework();
        commandFramework.addCustomParameter("Player", CommandArguments::<Player>getSender);
        commandFramework.addCustomParameter("Arena", arguments -> plugin.getArenaRegistry().getArena(arguments.getArgument(0)));
        commandFramework.addCustomParameter("pArena", arguments -> plugin.getArenaRegistry().getArena(arguments.<Player>getSender()));
    }

    public AbstractCommandHandler() {
        plugin.getCommandFramework().registerCommands(this);
    }
}
