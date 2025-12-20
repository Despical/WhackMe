package dev.despical.whackme.command;

import dev.despical.whackme.WhackMe;
import dev.despical.whackme.handler.ChatManager;

public abstract class CommandCategory {

    protected static final WhackMe plugin = WhackMe.getInstance();
    protected static final ChatManager chatManager = plugin.getChatManager();

    public CommandCategory() {
        plugin.getCommandFramework().registerCommands(this);
    }
}
