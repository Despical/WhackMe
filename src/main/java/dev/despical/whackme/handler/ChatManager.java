package dev.despical.whackme.handler;

import me.clip.placeholderapi.PlaceholderAPI;
import dev.despical.commandframework.Message;
import dev.despical.commons.configuration.ConfigUtils;
import dev.despical.commons.string.StringFormatUtils;
import dev.despical.commons.util.Strings;
import dev.despical.whackme.WhackMe;
import dev.despical.whackme.api.Reloadable;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.text.MessageFormat;
import java.util.List;
import java.util.stream.Stream;

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

        Message.setColorFormatter(Strings::format);
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
        path = dev.despical.commons.string.StringUtils.capitalize(path.replace('_', '-'), '-', '.');
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
        path = dev.despical.commons.string.StringUtils.capitalize(path.replace('_', '-'), '-', '.');
        return config.getStringList(path);
    }

    @Override
    public void reload() {
        this.config = ConfigUtils.getConfig(plugin, "messages");
        this.prefix = message("in_game.plugin_prefix");
        this.papiEnabled = plugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI");

        Stream.of(Message.SHORT_ARG_SIZE, Message.LONG_ARG_SIZE).forEach(message -> message.setMessage((command, arguments) -> {
            arguments.sendMessage(this.prefixedMessage("commands.correct_usage").replace("%usage%", command.usage()));
            return true;
        }));

        Message.NO_PERMISSION.setMessage((cmd, args) -> {
            String message = this.message("Commands.No-Permission");

            if (!message.isEmpty()) {
                args.sendMessage(message);
            }

            return true;
        });

        StringFormatUtils.setTimeFormat(this.message("In-Game.Timer-Format"));
    }
}
