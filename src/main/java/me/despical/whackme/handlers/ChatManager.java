package me.despical.whackme.handlers;

import me.clip.placeholderapi.PlaceholderAPI;
import me.despical.commandframework.Message;
import me.despical.commons.configuration.ConfigUtils;
import me.despical.commons.string.StringFormatUtils;
import me.despical.commons.util.Strings;
import me.despical.whackme.WhackMe;
import me.despical.whackme.api.Reloadable;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.text.MessageFormat;
import java.util.List;

/**
 * @author Despical
 * <p>
 * Created at 18.06.2022
 */
public class ChatManager implements Reloadable {

    private final WhackMe plugin;
    private String prefix;
    private boolean papiEnabled;
    private FileConfiguration config;

    public ChatManager(WhackMe plugin) {
        this.plugin = plugin;
        this.reload();

        Message.NO_PERMISSION.setMessage((cmd, args) -> {
            String message = this.message("Commands.No-Permission");

            if (!message.isEmpty()) {
                args.sendMessage(message);
            }

            return true;
        });
    }

    public boolean isPapiEnabled() {
        return papiEnabled;
    }

    public String coloredRawMessage(String message) {
        return Strings.format(message);
    }

    public String prefixedRawMessage(String message) {
        return prefix + coloredRawMessage(message);
    }

    public String message(String path) {
        path = me.despical.commons.string.StringUtils.capitalize(path.replace('_', '-'), '-', '.');
        return coloredRawMessage(config.getString(path));
    }

    public String getPrefix() {
        return prefix;
    }

    public String prefixedMessage(String path) {
        return prefix + message(path);
    }

    public String message(String path, Player player) {
        String returnString = message(path);
        returnString = formatPlaceholders(returnString, player);

        return returnString;
    }

    public String prefixedMessage(String message, Object... params) {
        return prefix + this.message(message, params);
    }

    public String message(String path, Object... params) {
        String message = this.message(path);
        return MessageFormat.format(message, params);
    }

    public String formatPlaceholders(String message, Player player) {
        String returnString = message;
        returnString = returnString.replace("%player%", player.getName());

        if (papiEnabled) {
            returnString = PlaceholderAPI.setPlaceholders(player, returnString);
        }

        return coloredRawMessage(returnString);
    }

    public List<String> getStringList(String path) {
        path = me.despical.commons.string.StringUtils.capitalize(path.replace('_', '-'), '-', '.');
        return config.getStringList(path);
    }

    @Override
    public void reload() {
        this.config = ConfigUtils.getConfig(plugin, "messages");
        this.prefix = message("in_game.plugin_prefix");
        this.papiEnabled = plugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI");

        StringFormatUtils.setTimeFormat(this.message("In-Game.Timer-Format"));
    }
}
