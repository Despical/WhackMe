package me.despical.whackme.commands;

import me.despical.commandframework.Command;
import me.despical.commandframework.CommandArguments;
import me.despical.commandframework.Completer;
import me.despical.commandframework.Confirmation;
import me.despical.commons.configuration.ConfigUtils;
import me.despical.commons.miscellaneous.MiscUtils;
import me.despical.commons.serializer.LocationSerializer;
import me.despical.whackme.WhackMe;
import me.despical.whackme.arena.Arena;
import me.despical.whackme.handlers.setup.SetupInventory;
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

public class AdminCommands extends AbstractCommand {

	public AdminCommands(WhackMe plugin) {
		super(plugin);
	}

	@Command(
		name = "wm.create",
		permission = "wm.admin.create",
		usage = "/wm create <arena name>",
		desc = "Creates a new arena with default configuration",
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

		String path = String.format("instances.%s.", id);
		FileConfiguration config = ConfigUtils.getConfig(plugin, "arenas");

		config.set(path + "ready", false);
		config.set(path + "custom", false);
		config.set(path + "startLocation", LocationSerializer.SERIALIZED_LOCATION);
		config.set(path + "endLocation", LocationSerializer.SERIALIZED_LOCATION);
		config.set(path + "portalLocations", Collections.EMPTY_LIST);
		config.set(path + "signs", Collections.EMPTY_LIST);

		ConfigUtils.saveConfig(plugin, config, "arenas");

		Arena arena = new Arena(id);
		arena.setReady(false);
		arena.setEndLocation(LocationSerializer.DEFAULT_LOCATION);
		arena.setStartLocation(LocationSerializer.DEFAULT_LOCATION);

		plugin.getArenaRegistry().registerArena(arena);
	}

	@Command(
		name = "wm.delete",
		permission = "wm.admin.delete",
		usage = "/wm delete <arena name>",
		desc = "Deletes arena with the current configuration",
		min = 1
	)
	@Confirmation(
		message = "§cAre you sure you want to do this action? " +
			      "Type the command again §6within 10 seconds §cto confirm!",
		expireAfter = 10
	)
	public void deleteCommand(Arena arena, CommandArguments arguments) {
		if (arena == null) {
			arguments.sendMessage(chatManager.prefixedMessage("commands.no_arena_like_that"));
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

		arguments.sendMessage(chatManager.prefixedMessage("commands.removed_game_instance"));
	}

	@Command(
		name = "wm.edit",
		permission = "wm.admin.edit",
		usage = "/wm edit <arena name>",
		desc = "Opens the arena editor",
		min = 1,
		senderType = Command.SenderType.PLAYER
	)
	public void editCommand(Arena arena, CommandArguments arguments) {
		if (arena == null) {
			arguments.sendMessage(chatManager.prefixedMessage("commands.no_arena_like_that"));
			return;
		}

		new SetupInventory(plugin, arena, arguments.getSender()).openInventory();
	}

	@SuppressWarnings("all")
	@Command(
		name = "wm.help",
		permission = "wm.admin.help",
		usage = "/wm help"
	)
	public void helpCommand(CommandArguments arguments) {
		final boolean isPlayer = arguments.isSenderPlayer();
		final CommandSender sender = arguments.getSender();

		arguments.sendMessage("");
		MiscUtils.sendCenteredMessage(sender, "&3&l---- Whack Me Commands ----");
		arguments.sendMessage("");

		for (final Command command : plugin.getCommandFramework().getSubCommands()) {
			final String usage = command.usage(), desc = command.desc();

			if (desc.isEmpty() || usage.isEmpty()) continue;

			if (isPlayer) {
				((Player) sender).spigot().sendMessage(new ComponentBuilder(ChatColor.DARK_GRAY + " • ")
					.append(usage)
					.color(ChatColor.AQUA)
					.event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, usage))
					.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(desc)))
					.create());
			} else {
				arguments.sendMessage(" &8• &b" + usage + " &3- &b" + desc);
			}
		}

		if (isPlayer) {
			final Player player = arguments.getSender();

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
		permission = "wm.admin.list",
		usage = "/wm list",
		desc = "Shows all of the existing arenas"
	)
	public void listCommand(CommandArguments arguments) {
		final Set<Arena> arenas = plugin.getArenaRegistry().getArenas();

		if (arenas.isEmpty()) {
			arguments.sendMessage(chatManager.prefixedMessage("commands.list_command.no_arenas_created"));
			return;
		}

		arguments.sendMessage(chatManager.prefixedMessage("commands.list_command.format").replace("%list%", arenas.stream().map(Arena::getId).collect(Collectors.joining(", "))));
	}

	@Command(
		name = "wm.kick",
		permission = "wm.admin.kick",
		usage = "/wm kick <player>",
		desc = "Kicks specified player if they're playing",
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
		permission = "wm.admin.reload",
		usage = "/wm reload",
		desc = "Reloads arenas and configuration files"
	)
	public void reloadCommand(CommandArguments arguments) {
		plugin.reload();

		for (Arena arena : plugin.getArenaRegistry().getArenas()) {
			final Player player = arena.getPlayer();

			if (player != null) {
				player.setFlySpeed(.1F);
				player.setWalkSpeed(.2F);

				arena.removePlayer();
				arena.teleportToEndLocation();
			}
		}

		plugin.getArenaRegistry().registerArenas();
		arguments.sendMessage(chatManager.prefixedMessage("commands.success_reload"));
	}

	@Completer(
		name = "wm"
	)
	public List<String> onTabComplete(CommandArguments arguments) {
		final List<String> completions = new ArrayList<>(), commands = plugin.getCommandFramework().getSubCommands().stream().map(cmd -> cmd.name().replace(arguments.getLabel() + '.', "")).collect(Collectors.toList());
		final String[] args = arguments.getArguments();

		if (args.length > 0) {
			if (Arrays.asList("create", "list", "help", "reload", "leave", "randomjoin").contains(args[0])) {
				return completions;
			}
		}

		if (args.length == 1) {
			return StringUtil.copyPartialMatches(args[0], arguments.hasPermission("wm.admin") ? commands : Arrays.asList("join", "randomjoin", "leave", "top", "stats"), completions);
		}

		final String arg = args[0];

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
}