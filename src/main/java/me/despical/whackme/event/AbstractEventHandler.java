package me.despical.whackme.event;

import me.despical.whackme.WhackMe;
import me.despical.whackme.handlers.ChatManager;
import org.bukkit.event.Listener;

/**
 * @author Despical
 * <p>
 * Created at 22.06.2022
 */
public abstract class AbstractEventHandler implements Listener {

    protected static final WhackMe plugin = WhackMe.getInstance();
    protected static final ChatManager chatManager = plugin.getChatManager();

    public AbstractEventHandler() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
}
