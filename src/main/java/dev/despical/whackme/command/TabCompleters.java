package dev.despical.whackme.command;

import dev.despical.commandframework.CompleterHelper;
import dev.despical.commandframework.annotations.Completer;
import dev.despical.commandframework.debug.Debug;
import dev.despical.whackme.option.BooleanOption;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Despical
 * <p>
 * Created at 11.12.2025
 */
public final class TabCompleters extends CommandCategory {

    @Completer(
        name = "whackme",
        aliases = "wm",
        permission = "whackme.command.tabcompleter"
    )
    public List<String> onTabCompletion(Arguments arguments, CompleterHelper helper) {
        int length = arguments.getLength();
        List<String> availableCommands = collectAvailableCommands(arguments);

        return switch (length) {
            case 0 -> availableCommands;
            case 1 -> helper.copyMatches(0, availableCommands);
            case 2 -> {
                if (helper.equalsAny(0, "edit", "delete", "join", "stop")) {
                    yield helper.copyMatches(1, arenaRegistry.getArenaNames());
                }

                if (helper.equalsAny(0, "stats")) {
                    yield helper.copyMatches(1, helper.playerNames());
                }

                if (helper.equalsAny(0, "kick") && arguments.hasPermission("whackme.admin.kick")) {
                    yield helper.copyMatches(1, helper.playerNames());
                }

                yield helper.empty();
            }
            default -> helper.empty();
        };
    }

    @Debug
    @Completer(
        name = "whackme.debug",
        aliases = "wm.debug",
        permission = "whackme.debug.tabcompleter"
    )
    public List<String> debugTabCompleter(Arguments arguments, CompleterHelper helper) {
        if (arguments.isSenderConsole()) {
            return helper.empty();
        }

        if (arguments.getLength() == 1) {
            return helper.copyMatches(0, List.of("component", "dump"));
        }

        if (helper.equalsAny(0, "join") && arguments.getLength() == 2) {
            return helper.copyMatches(1, arenaRegistry.getArenaNames());
        }

        return helper.empty();
    }

    private List<String> collectAvailableCommands(Arguments arguments) {
        List<String> availableCommands = new ArrayList<>(List.of("join", "leave", "stats"));

        if (arguments.hasPermission("whackme.command.help")) {
            availableCommands.add("help");
        }

        if (arguments.hasPermission("whackme.arena.create")) {
            availableCommands.add("create");
        }

        if (arguments.hasPermission("whackme.arena.list")) {
            availableCommands.add("list");
        }

        if (arguments.hasPermission("whackme.arena.edit")) {
            availableCommands.add("edit");
        }

        if (arguments.hasPermission("whackme.arena.delete")) {
            availableCommands.add("delete");
        }

        if (arguments.hasPermission("whackme.admin.stop")) {
            availableCommands.add("stop");
        }

        if (arguments.hasPermission("whackme.admin.reload")) {
            availableCommands.add("reload");
        }

        if (arguments.hasPermission("whackme.admin.kick")) {
            availableCommands.add("kick");
        }

        if (BooleanOption.DEBUG.value() && arguments.hasPermission("whackme.debug.tabcompleter")) {
            availableCommands.add("debug");
        }

        return availableCommands;
    }
}
