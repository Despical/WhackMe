package dev.despical.whackme.command;

import dev.despical.commandframework.CommandArguments;
import dev.despical.whackme.WhackMe;
import dev.despical.whackme.user.User;
import dev.despical.whackme.util.Var;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

/**
 * @author Despical
 * <p>
 * Created at 31.07.2026
 */
public final class Arguments extends CommandArguments {

    private static final WhackMe plugin = WhackMe.getInstance();

    private final CommandArguments arguments;

    public Arguments(CommandArguments arguments) {
        super(arguments);
        this.arguments = arguments;
    }

    public User getUser() {
        return plugin.getUserManager().getUser(arguments.<Player>getSender());
    }

    public void sendConfiguredMessage(String messageKey, Var... vars) {
        plugin.getChatManager().sendMessage(this, messageKey, vars);
    }

    public void sendBlankMessage() {
        arguments.sendMessage("");
    }

    public void sendRawMessage(String message, Var... vars) {
        plugin.getChatManager().sendRawMessage(arguments.getSender(), message, vars);
    }

    public void sendCenteredMessage(String path, Var... vars) {
        plugin.getChatManager().sendCenteredMessage(arguments.getSender(), path, vars);
    }

    public void playSound(Sound sound, float volume, float pitch) {
        Player player = arguments.getSender();
        player.playSound(player.getLocation(), sound, volume, pitch);
    }
}
