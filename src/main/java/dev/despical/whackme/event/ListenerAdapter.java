package dev.despical.whackme.event;

import dev.despical.whackme.WhackMe;
import dev.despical.whackme.arena.ArenaManager;
import dev.despical.whackme.arena.ArenaRegistry;
import dev.despical.whackme.chat.ChatManager;
import dev.despical.whackme.option.ConfigOptions;
import dev.despical.whackme.user.UserManager;
import org.bukkit.event.Listener;

/**
 * @author Despical
 * <p>
 * Created at 01.06.2026
 */
public abstract class ListenerAdapter implements Listener {

    protected static final WhackMe plugin = WhackMe.getInstance();

    protected final ConfigOptions options;
    protected final ArenaManager arenaManager;
    protected final ArenaRegistry arenaRegistry;
    protected final UserManager userManager;
    protected final ChatManager chatManager;

    public ListenerAdapter() {
        this.options = plugin.getOptions();
        this.arenaManager = plugin.getArenaManager();
        this.arenaRegistry = plugin.getArenaRegistry();
        this.userManager = plugin.getUserManager();
        this.chatManager = plugin.getChatManager();

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
}
