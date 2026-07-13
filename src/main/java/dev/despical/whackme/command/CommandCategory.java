package dev.despical.whackme.command;

import dev.despical.whackme.WhackMe;
import dev.despical.whackme.arena.ArenaManager;
import dev.despical.whackme.arena.ArenaRegistry;
import dev.despical.whackme.chat.ChatManager;
import dev.despical.whackme.user.UserManager;

/**
 * @author Despical
 * <p>
 * Created at 6.12.2025
 */
public abstract sealed class CommandCategory permits ArenaCommands, AdminCommands, PlayerCommands, DebugCommands, TabCompleters {

    protected static final WhackMe plugin = WhackMe.getInstance();
    protected static final ArenaRegistry arenaRegistry = plugin.getArenaRegistry();
    protected static final ArenaManager arenaManager = plugin.getArenaManager();
    protected static final ChatManager chatManager = plugin.getChatManager();
    protected static final UserManager userManager = plugin.getUserManager();
}
