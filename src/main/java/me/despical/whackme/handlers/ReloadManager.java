package me.despical.whackme.handlers;

import me.despical.whackme.WhackMe;
import me.despical.whackme.api.Reloadable;
import org.bukkit.command.CommandSender;

import java.lang.reflect.Field;
import java.util.logging.Level;

/**
 * @author Despical
 * <p>
 * Created at 20.04.2024
 */
public final class ReloadManager {

    private final WhackMe plugin;

    public ReloadManager(WhackMe plugin) {
        this.plugin = plugin;
    }

    public void initializeReload(CommandSender sender) {
        sendMessage(sender, "Initialized", sender.getName());

        try {
            plugin.reloadConfig();

            for (Field field : plugin.getClass().getDeclaredFields()) {
                if (Reloadable.class.isAssignableFrom(field.getType())) {
                    field.setAccessible(true);

                    if (field.get(plugin) instanceof Reloadable) {
                        ((Reloadable) field.get(plugin)).reload();
                    }
                }
            }
        } catch (Exception exception) {
            plugin.getLogger().log(Level.WARNING, "Error occurred during the reload!", exception);

            sendMessage(sender, "Error-Occurred");
        }

        sendMessage(sender, "Finished");
    }

    private void sendMessage(CommandSender sender, String path, Object... params) {
        String message = plugin.getChatManager().prefixedMessage("Commands.Reload-Command." + path, params);

        sender.sendMessage(message);
    }
}
