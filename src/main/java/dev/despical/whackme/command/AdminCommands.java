package dev.despical.whackme.command;

import dev.despical.commandframework.CommandArguments;
import dev.despical.commandframework.annotations.Command;
import dev.despical.commandframework.annotations.Completer;
import dev.despical.commons.configuration.ConfigUtils;
import dev.despical.commons.miscellaneous.MiscUtils;
import dev.despical.commons.serializer.LocationSerializer;
import dev.despical.commons.util.Strings;
import dev.despical.whackme.arena.Arena;
import dev.despical.whackme.handler.setup.SetupInventory;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.*;
import java.util.stream.Collectors;

public class AdminCommands extends CommandCategory {

    @Command(
        name = "wm.create",
        usage = "/wm create <arena name>",
        desc = "Creates a new arena with default configuration.",
        permission = "wm.admin.create",
        senderType = Command.SenderType.PLAYER
    )
    public void createCommand(CommandArguments arguments) {
        if (arguments.isArgumentsEmpty()) {
            arguments.sendMessage(chatManager.prefixedRawMessage("&cPlease enter an name to create an arena!"));
            return;
        }

        String id = arguments.getArgument(0);

        if (plugin.getArenaRegistry().isArena(id)) {
            arguments.sendMessage(chatManager.prefixedRawMessage("&cArena with that ID already contains!"));
            arguments.sendMessage(chatManager.prefixedRawMessage("&cTo check existing arenas use: /wm list"));
            return;
        }

        Player player = arguments.getSender();

        arguments.sendMessage("&l--------------------------------------------");
        MiscUtils.sendCenteredMessage(player, "&eInstance &a&l" + id + " &ecreated!");
        arguments.sendMessage("");
        MiscUtils.sendCenteredMessage(player, "&aEdit this arena via /wm edit &6" + id + "&a!");
        arguments.sendMessage("");
        MiscUtils.sendCenteredMessage(player, "&6Don't know where to start? Check out our video:");
        MiscUtils.sendCenteredMessage(player, "&7https://www.youtube.com/watch?v=fOw5AQ8A-Jk");
        arguments.sendMessage("&l--------------------------------------------");

        Arena arena = new Arena(id);
        arena.setEndLocation(LocationSerializer.DEFAULT_LOCATION);
        arena.setStartLocation(LocationSerializer.DEFAULT_LOCATION);

        saveArenaData(arena);

        plugin.getArenaRegistry().registerArena(arena);
    }

    private void saveArenaData(Arena arena) {
        String path = String.format("instances.%s.", arena.getId());
        FileConfiguration config = ConfigUtils.getConfig(plugin, "arenas");

        config.set(path + "ready", false);
        config.set(path + "custom", false);
        config.set(path + "startLocation", LocationSerializer.SERIALIZED_LOCATION);
        config.set(path + "endLocation", LocationSerializer.SERIALIZED_LOCATION);
        config.set(path + "minPoints", 4);
        config.set(path + "maxPoints", 8);
        config.set(path + "portalLocations", Collections.EMPTY_LIST);
        config.set(path + "signs", Collections.EMPTY_LIST);

        ConfigUtils.saveConfig(plugin, config, "arenas");
    }

    @Command(
        name = "wm.delete",
        usage = "/wm delete <arena name>",
        desc = "Deletes arena with the current configuration.",
        permission = "wm.admin.delete",
        min = 1
    )
    public void deleteCommand(Arena arena, CommandArguments arguments) {
        if (arena == null) {
            arguments.sendMessage(chatManager.prefixedMessage("Commands.No-Arena-Like-That"));
            return;
        }

        if (arena.getPlayer() != null) {
            arena.removePlayer();
            arena.teleportToEndLocation();
        }

        plugin.getSignManager().removeSigns(arena);
        plugin.getArenaRegistry().unregisterArena(arena);

        FileConfiguration config = ConfigUtils.getConfig(plugin, "arenas");

        config.set("instances." + arguments.getArgument(0), null);
        ConfigUtils.saveConfig(plugin, config, "arenas");

        arguments.sendMessage(chatManager.prefixedMessage("Commands.Removed-Game-Instance"));
    }

    @Command(
        name = "wm.edit",
        usage = "/wm edit <arena name>",
        desc = "Opens the arena editor.",
        permission = "wm.admin.edit",
        min = 1,
        senderType = Command.SenderType.PLAYER
    )
    public void editCommand(Arena arena, CommandArguments arguments) {
        if (arena == null) {
            arguments.sendMessage(chatManager.prefixedMessage("Commands.No-Arena-Like-That"));
            return;
        }

        new SetupInventory(plugin, arena, arguments.getSender());
    }

    @SuppressWarnings("all")
    @Command(
        name = "wm.help",
        usage = "/wm help",
        permission = "wm.admin.help"
    )
    public void helpCommand(CommandArguments arguments) {
        boolean isPlayer = arguments.isSenderPlayer();
        CommandSender sender = arguments.getSender();

        arguments.sendMessage("");
        MiscUtils.sendCenteredMessage(sender, "&3&lWhack Me");
        MiscUtils.sendCenteredMessage(arguments.getSender(), "&3[&boptional argument&3] &b- &3<&brequired argument&3>");
        arguments.sendMessage("");

        for (Command command : plugin.getCommandFramework().getSubCommands()) {
            String usage = formatCommandUsage("&3" + command.usage()), desc = command.desc();

            if (desc.isEmpty()) continue;

            if (isPlayer) {
                ((Player) sender).spigot().sendMessage(
                    new ComponentBuilder(ChatColor.DARK_GRAY + " • ")
                        .append(usage)
                        .color(ChatColor.AQUA)
                        .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command.usage()))
                        .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(desc)))
                        .create());
            } else {
                arguments.sendMessage(" &8• &b" + usage + " &3- &b" + desc);
            }
        }

        if (isPlayer) {
            Player player = arguments.getSender();

            player.sendMessage("");
            player.spigot().sendMessage(new ComponentBuilder("TIP:").color(ChatColor.YELLOW).bold(true)
                .append(" Try to ", ComponentBuilder.FormatRetention.NONE).color(ChatColor.GRAY)
                .append("hover").color(ChatColor.WHITE).underlined(true)
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(ChatColor.LIGHT_PURPLE + "Hover on the commands to get info about them.")))
                .append(" or ", ComponentBuilder.FormatRetention.NONE).color(ChatColor.GRAY)
                .append("click").color(ChatColor.WHITE).underlined(true)
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(ChatColor.LIGHT_PURPLE + "Click on the commands to insert them in the chat.")))
                .append(" on the commands!", ComponentBuilder.FormatRetention.NONE).color(ChatColor.GRAY)
                .create());
        }
    }

    @Command(
        name = "wm.list",
        usage = "/wm list",
        desc = "Shows all of the existing arenas.",
        permission = "wm.admin.list"
    )
    public void listCommand(CommandArguments arguments) {
        Set<Arena> arenas = plugin.getArenaRegistry().getArenas();

        if (arenas.isEmpty()) {
            arguments.sendMessage(chatManager.prefixedMessage("commands.list_command.no_arenas_created"));
            return;
        }

        arguments.sendMessage(chatManager.prefixedMessage("commands.list_command.format").replace("%list%", arenas.stream().map(Arena::getId).collect(Collectors.joining(", "))));
    }

    @Command(
        name = "wm.kick",
        usage = "/wm kick <arena name>",
        desc = "Kicks specified player if they're playing.",
        permission = "wm.admin.kick",
        min = 1
    )
    public void kickCommand(Arena arena, CommandArguments arguments) {
        if (arena == null) {
            arguments.sendMessage(chatManager.prefixedMessage("commands.no_arena_like_that"));
            return;
        }

        if (arena.getPlayer() != null) {
            arena.removePlayer();

            arguments.sendMessage(chatManager.prefixedMessage("commands.kicked_player"));
            return;
        }

        arguments.sendMessage(chatManager.prefixedMessage("commands.no_one_playing"));
    }

    @Command(
        name = "wm.reload",
        usage = "/wm reload",
        desc = "Reloads arenas and configuration files",
        permission = "wm.admin.reload"
    )
    public void reloadCommand(CommandArguments arguments) {
        plugin.getReloadManager().initializeReload(arguments.getSender());

        for (Arena arena : plugin.getArenaRegistry().getArenas()) {
            Player player = arena.getPlayer();

            if (player != null) {
                player.setFlySpeed(.1F);
                player.setWalkSpeed(.2F);

                arena.removePlayer();
                arena.teleportToEndLocation();
            }
        }

        plugin.getArenaRegistry().registerArenas();
    }

    @Command(
        name = "wm.time",
        usage = "/wm time <arena> <add | remove | set> <value>",
        desc = "Manipulates the timer of target arena.",
        onlyOp = true
    )
    public void timeCommand(Arena arena, CommandArguments arguments) {
        String usage = chatManager.prefixedMessage("commands.time_command.usage");

        if (arguments.getLength() < 3) {
            arguments.sendMessage(usage);
            return;
        }

        if (arena == null) {
            arguments.sendMessage(chatManager.prefixedMessage("commands.no_arena_like_that"));
            return;
        }

        if (arena.getPlayer() == null) {
            arguments.sendMessage(chatManager.prefixedMessage("commands.time_command.arena_is_empty"));
            return;
        }

        String argument = arguments.getArgument(1, "invalid_args");
        int value = Math.abs(arguments.getArgumentAsInt(2));
        int current = arena.getTimer();

        switch (argument) {
            case "add":
                arena.setTimer(current + value);
                break;
            case "remove":
                arena.setTimer(Math.max(0, current - value));
                break;
            case "set":
                arena.setTimer(Math.max(0, value));
                break;
            default:
                arguments.sendMessage(usage);
        }
    }

    @Command(
        name = "wm.version",
        usage = "/wm version",
        desc = "Displays detailed information about the plugin and server environment.",
        permission = "wm.admin.version"
    )
    public void infoCommand(CommandArguments arguments) {
        CommandSender sender = arguments.getSender();

        arguments.sendMessage("");
        MiscUtils.sendCenteredMessage(sender, "&b&l==== [ &3&lWhack Me &b&l] ==== ");
        arguments.sendMessage("");
        arguments.sendMessage(" &8• &3Plugin Version: &b{0}", plugin.getDescription().getVersion());
        arguments.sendMessage(" &8• &3Server Version: &b{0}", plugin.getServer().getVersion());
        arguments.sendMessage(" &8• &3Bukkit Version: &b{0}", plugin.getServer().getBukkitVersion());
        arguments.sendMessage(" &8• &3Loaded Plugins: &b{0}", plugin.getServer().getPluginManager().getPlugins().length);
        arguments.sendMessage("");
        arguments.sendMessage(" &8• &3Java Version: &b{0}", System.getProperty("java.version"));
        arguments.sendMessage(" &8• &3Java Vendor: &b{0}", System.getProperty("java.vendor"));
        arguments.sendMessage(" &8• &3JVM Version: &b{0}", System.getProperty("java.vm.version"));
        arguments.sendMessage(" &8• &3JVM Name: &b{0}", System.getProperty("java.vm.name"));
        arguments.sendMessage("");
        arguments.sendMessage(" &8• &3OS Name: &b{0} ({1})", System.getProperty("os.name"), System.getProperty("os.arch"));
        arguments.sendMessage("");
    }

    @Completer(
        name = "wm"
    )
    public List<String> onTabComplete(CommandArguments arguments) {
        List<String> completions = new ArrayList<>(), commands = plugin.getCommandFramework().getSubCommands().stream().map(cmd -> cmd.name().replace(arguments.getLabel() + '.', "")).collect(Collectors.toList());
        String[] args = arguments.getArguments();

        if (args.length > 0) {
            if (Arrays.asList("create", "list", "help", "reload", "leave", "randomjoin").contains(args[0])) {
                return completions;
            }
        }

        if (args.length == 1) {
            return StringUtil.copyPartialMatches(args[0], arguments.hasPermission("wm.admin") ? commands : Arrays.asList("join", "randomjoin", "leave", "top", "stats"), completions);
        }

        String arg = args[0];

        if (args.length == 2) {
            if (arg.equalsIgnoreCase("top")) {
                return StringUtil.copyPartialMatches(args[1], Arrays.asList("tours_played", "record_score"), completions);
            }

            if (arg.equalsIgnoreCase("stats")) {
                return plugin.getServer().getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
            }

            if (!commands.contains(arg)) {
                return completions;
            }

            if (arg.equalsIgnoreCase("create")) return null;
            if (!arguments.hasPermission("wm.admin") && !arg.equalsIgnoreCase("join")) return null;

            List<String> arenas = plugin.getArenaRegistry().getArenas().stream().map(Arena::getId).collect(Collectors.toList());
            return StringUtil.copyPartialMatches(args[1], arenas, completions);
        }

        return completions;
    }

    private String formatCommandUsage(String usage) {
        char[] array = usage.toCharArray();
        StringBuilder buffer = new StringBuilder(usage);

        for (int i = 0; i < array.length; i++) {
            if (array[i] == '[' || array[i] == '<') {
                buffer.insert(i, "&b");
                return Strings.format(buffer.toString());
            }
        }

        return Strings.format(usage);
    }
}
