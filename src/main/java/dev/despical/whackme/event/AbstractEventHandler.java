package dev.despical.whackme.event;

import dev.despical.whackme.WhackMe;
import dev.despical.whackme.handler.ChatManager;
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
