package dev.despical.whackme.chat;

import dev.despical.commandframework.CommandArguments;
import dev.despical.commons.configuration.ConfigUtils;
import dev.despical.whackme.WhackMe;
import dev.despical.whackme.util.StringUtils;
import dev.despical.whackme.util.Utils;
import dev.despical.whackme.util.Var;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Despical
 * <p>
 * Created at 18.06.2022
 */
public class ChatManager {

    private static final String NO_CENTER_PREFIX = "%no_center%";

    private final WhackMe plugin;
    private final MiniMessage miniMessage;
    private final Map<String, Component> messages;

    private FileConfiguration messagesFile;

    public ChatManager(WhackMe plugin) {
        this.plugin = plugin;
        this.miniMessage = MiniMessage.miniMessage();
        this.messages = new HashMap<>();
        this.loadFile();
    }

    public void loadFile() {
        this.messages.clear();
        this.messagesFile = ConfigUtils.getConfig(plugin, "messages");
    }

    public void sendRawComponent(Player player, Component component, Var... vars) {
        if (component == Component.empty()) {
            return;
        }

        player.sendMessage(replaceVarsInComponent(component, vars));
    }

    public void sendMessage(CommandArguments arguments, String messageKey, Var... vars) {
        Component component = getMessageComponent(messageKey, vars);
        if (component == Component.empty()) {
            return;
        }

        arguments.sendMessage(component);
    }

    public void sendMessage(CommandSender recipient, String messageKey, Var... vars) {
        Component component = getMessageComponent(messageKey, vars);
        if (component == Component.empty()) {
            return;
        }

        recipient.sendMessage(component);
    }

    public void sendCenteredMessage(CommandSender recipient, String path, Var... vars) {
        List<String> configuredMessages = messagesFile.getStringList(path);
        if (!configuredMessages.isEmpty()) {
            configuredMessages.stream()
                .map(line -> Utils.format(line, vars))
                .forEach(message -> sendPossiblyCenteredMessage(recipient, message, vars));
            return;
        }

        String singleMessage = messagesFile.getString(path);
        if (singleMessage != null && !singleMessage.isEmpty()) {
            sendPossiblyCenteredMessage(recipient, Utils.format(singleMessage, vars), vars);
        }
    }

    public void sendCenteredMessage(CommandSender recipient, List<String> messages, Var... vars) {
        messages.forEach(message -> sendPossiblyCenteredMessage(recipient, message, vars));
    }

    private void sendPossiblyCenteredMessage(CommandSender recipient, String message, Var... vars) {
        if (message.startsWith(NO_CENTER_PREFIX)) {
            recipient.sendMessage(parseMessage(message.substring(NO_CENTER_PREFIX.length()), vars));
            return;
        }

        StringUtils.sendCenteredMessage(recipient, parseMessage(message, vars));
    }

    public void sendRawMessage(CommandSender recipient, String message, Var... vars) {
        recipient.sendMessage(parseMessage(message, vars));
    }

    public void sendActionBar(CommandSender recipient, String messageKey, Var... vars) {
        recipient.sendActionBar(getMessageComponent(messageKey, vars));
    }

    public void sendRawActionBar(CommandSender recipient, String message, Var... vars) {
        recipient.sendActionBar(parseMessage(message, vars));
    }

    public Component getMessageComponent(String messageKey, Var... vars) {
        String configMessage = getString(messageKey);
        Component component = messages.computeIfAbsent(messageKey, str -> parseMessage(configMessage));

        return replaceVarsInComponent(component, vars);
    }

    public String getRawString(String path, Var... variables) {
        String string = messagesFile.getString(path, "");
        for (Var var : variables) {
            string = string.replace(var.name, (String) var.value);
        }

        return string;
    }

    public Component parseMessage(String message, Var... vars) {
        if (message == null) {
            return Component.empty();
        }

        return replaceVarsInComponent(miniMessage.deserialize(message), vars);
    }

    public List<Component> parseList(List<String> list, Var... vars) {
        return list.stream().map(message -> parseMessage(message, vars)).toList();
    }

    public Component replaceVarsInComponent(Component component, Var... vars) {
        for (Var var : vars) {
            Component replacementComponent = miniMessage.deserialize(var.value.toString());
            component = component.replaceText(builder ->
                builder
                    .matchLiteral(var.name)
                    .replacement(replacementComponent)
            );
        }

        return component;
    }

    public ConfigurationSection getConfigSection(String path) {
        return messagesFile.getConfigurationSection(path);
    }

    public List<String> getStringList(String path) {
        return messagesFile.getStringList(path);
    }

    public List<Component> getComponentList(String path) {
        return messagesFile.getStringList(path)
            .stream()
            .map(this::parseMessage)
            .toList();
    }

    private String getString(String path) {
        if (messagesFile.isList(path)) {
            return String.join("\n", messagesFile.getStringList(path));
        }

        return getRawString(path);
    }
}
