package dev.despical.whackme.command;

import dev.despical.whackme.WhackMe;
import dev.despical.whackme.option.BooleanOption;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * @author Despical
 * <p>
 * Created at 13.07.2026
 */
public final class PlayingCommandPolicy {

    public static final String BYPASS_PERMISSION = "whackme.commandblock.bypass";

    private final WhackMe plugin;
    private Set<String> allowedCommands = Set.of();
    private Set<String> allowedRootCommands = Set.of();

    public PlayingCommandPolicy(WhackMe plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        Set<String> commands = new HashSet<>();
        Set<String> roots = new HashSet<>();

        for (String configured : plugin.getConfig().getStringList("whitelisted-commands")) {
            addCommand(configured, commands, roots);
        }
        for (String configured : plugin.getConfig().getStringList("command-settings.allowed-commands")) {
            addCommand(configured, commands, roots);
        }

        if (!BooleanOption.BLOCK_LEAVE_COMMAND.value()) {
            commands.add("wm leave");
            commands.add("whackme leave");
        }

        allowedCommands = Set.copyOf(commands);
        allowedRootCommands = Set.copyOf(roots);
    }

    public boolean isCommandAllowed(String command) {
        String normalized = normalize(command);
        if (normalized.isEmpty() || allowedCommands.contains("*") || allowedCommands.contains(normalized)
            || allowedRootCommands.contains(root(normalized))) {
            return true;
        }

        return allowedCommands.stream().anyMatch(allowed -> normalized.startsWith(allowed + " "));
    }

    private void addCommand(String command, Set<String> commands, Set<String> roots) {
        String normalized = normalize(command);
        if (normalized.isEmpty()) {
            return;
        }
        commands.add(normalized);
        if (!normalized.contains(" ")) {
            roots.add(normalized);
        }
    }

    private String normalize(String command) {
        if (command == null) {
            return "";
        }

        String trimmed = command.trim();
        while (trimmed.startsWith("/")) {
            trimmed = trimmed.substring(1).trim();
        }
        if (trimmed.isEmpty()) {
            return "";
        }

        String[] parts = trimmed.toLowerCase(Locale.ENGLISH).split("\\s+");
        int namespace = parts[0].indexOf(':');
        if (namespace >= 0) {
            parts[0] = parts[0].substring(namespace + 1);
        }
        return String.join(" ", parts);
    }

    private String root(String command) {
        int space = command.indexOf(' ');
        return space < 0 ? command : command.substring(0, space);
    }
}
