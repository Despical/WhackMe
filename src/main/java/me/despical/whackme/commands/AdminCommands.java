package me.despical.whackme.commands;

import me.despical.commandframework.Command;
import me.despical.commandframework.CommandArguments;
import me.despical.commandframework.Completer;
import me.despical.commons.configuration.ConfigUtils;
import me.despical.commons.miscellaneous.MiscUtils;
import me.despical.commons.serializer.LocationSerializer;
import me.despical.whackme.WhackMe;
import me.despical.whackme.arena.Arena;
import me.despical.whackme.handlers.setup.SetupInventory;
import me.despical.whackme.user.User;
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

import static me.despical.commandframework.Command.SenderType.PLAYER;

public class AdminCommands extends AbstractCommand {

	private final Set<CommandSender> confirmations;

	public AdminCommands(WhackMe plugin) {
		super(plugin);
		this.confirmations = new HashSet<>();
	}

	@Command(
		name = "wm.create",
		permission = "wm.admin.create",
		usage = "/wm create <arena name>",
		desc = "Creates a new arena with default configuration",
		senderType = PLAYER
	)
	public void createCommand(CommandArguments arguments) {
		final Player player = arguments.getSender();
		final User user = plugin.getUserManager().getUser(player);

		if (arguments.isArgumentsEmpty()) {
			user.sendRawMessage("&cPlease enter an name to create an arena!");
			return;
		}

		final String id = arguments.getArgument(0);

		if (plugin.getArenaRegistry().isArena(id)) {
			user.sendRawMessage("&cArena with that ID already contains!");
			user.sendRawMessage("&cTo check existing arenas use: /wm list");
			return;
		}

		user.sendRawMessage("&l--------------------------------------------");
		MiscUtils.sendCenteredMessage(player, "&eInstance &a&l" + id + " &ecreated!");
		user.sendRawMessage("");
		MiscUtils.sendCenteredMessage(player, "&aEdit this arena via /wm edit &6" + id + "&a!");
		user.sendRawMessage("");
		MiscUtils.sendCenteredMessage(player, "&6Don't know where to start? Check out our video:");
		MiscUtils.sendCenteredMessage(player, "&7https://www.youtube.com/watch?v=fOw5AQ8A-Jk");
		user.sendRawMessage("&l--------------------------------------------");

		final String path = String.format("instances.%s.", id);
		final FileConfiguration config = ConfigUtils.getConfig(plugin, "arenas");

		config.set(path + "ready", false);
		config.set(path + "custom", false);
		config.set(path + "startLocation", LocationSerializer.SERIALIZED_LOCATION);
		config.set(path + "endLocation", LocationSerializer.SERIALIZED_LOCATION);
		config.set(path + "portalLocations", new ArrayList<>());

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
	public void deleteCommand(CommandArguments arguments) {
		final String arenaId = arguments.getArgument(0);
		final Arena arena = plugin.getArenaRegistry().getArena(arenaId);
		final CommandSender sender = arguments.getSender();

		if (arena == null) {
			arguments.sendMessage(chatManager.prefixedMessage("commands.no_arena_like_that"));
			return;
		}

		if (!confirmations.contains(sender)) {
			confirmations.add(sender);
			plugin.getServer().getScheduler().runTaskLater(plugin, () -> confirmations.remove(sender), 200);
			arguments.sendMessage(chatManager.prefixedMessage("commands.are_you_sure"));
			return;
		}

		confirmations.remove(sender);

		final Player player = arena.getPlayer();

		if (player != null) {
			arena.removePlayer();
			arena.teleportToEndLocation();
		}

		plugin.getArenaRegistry().unregisterArena(arena);

		final FileConfiguration config = ConfigUtils.getConfig(plugin, "arenas");

		config.set("instances." + arenaId, null);
		ConfigUtils.saveConfig(plugin, config, "arenas");

		sender.sendMessage(chatManager.prefixedMessage("commands.removed_game_instance"));
	}

	@Command(
		name = "wm.edit",
		permission = "wm.admin.edit",
		usage = "/wm edit <arena name>",
		desc = "Opens the arena editor",
		senderType = PLAYER
	)
	public void editCommand(CommandArguments arguments) {
		final Player player = arguments.getSender();
		final Arena arena = plugin.getArenaRegistry().getArena(arguments.getArgument(0));

		if (arena == null) {
			player.sendMessage(chatManager.prefixedMessage("commands.no_arena_like_that"));
			return;
		}

		new SetupInventory(plugin, arena, player).openInventory();
	}

	@SuppressWarnings("all")
	@Command(
		name = "wm.help",
		permission = "wm.admin.help"
	)
	public void helpCommand(CommandArguments arguments) {
		final boolean isPlayer = arguments.isSenderPlayer();
		final String header = chatManager.coloredRawMessage("&3&l---- Whack Me Admin Commands ----");
		final CommandSender sender = arguments.getSender();

		arguments.sendMessage("");
		MiscUtils.sendCenteredMessage(sender, header);
		arguments.sendMessage("");

		for (final Command command : plugin.getCommandFramework().getCommands().stream().sorted(Collections
			.reverseOrder(Comparator.comparingInt(cmd -> cmd.usage().length()))).collect(Collectors.toList())) {
			final String usage = command.usage(), desc = command.desc();

			if (usage.isEmpty()) continue;

			if (isPlayer) {
				((Player) sender).spigot().sendMessage(new ComponentBuilder()
					.color(ChatColor.DARK_GRAY)
					.append(" • ")
					.append(usage)
					.color(ChatColor.AQUA)
					.event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, usage))
					.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(desc)))
					.create());
			} else {
				sender.sendMessage(chatManager.coloredRawMessage(" &8• &b" + usage + " &3- &b" + desc));
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
		desc = "Kicks specified player if they're playing"
	)
	public void kickCommand(CommandArguments arguments) {
		final Arena arena = plugin.getArenaRegistry().getArena(arguments.getArgument(0));

		if (arena == null) {
			arguments.sendMessage(chatManager.prefixedMessage("commands.no_arena_like_that"));
			return;
		}

		arena.removePlayer();
	}

	@Command(
		name = "wm.reload",
		permission = "wm.admin.reload",
		usage = "/wm reload",
		desc = "Kicks specified player if they're playing"
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
		final List<String> completions = new ArrayList<>(), commands = plugin.getCommandFramework().getCommands().stream().map(cmd -> cmd.name().replace(arguments.getLabel() + '.', "")).collect(Collectors.toList());
		final String args[] = arguments.getArguments(), arg = args[0];

		commands.remove("wm");

		if (args.length == 1) {
			StringUtil.copyPartialMatches(arg, commands, completions);
		}

		if (args.length == 2) {
			if (arg.equalsIgnoreCase("top")) {
				return Arrays.asList("tours_played", "record_score");
			}

			if (arg.equalsIgnoreCase("stats")) {
				return plugin.getServer().getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
			}

			if (!commands.contains(arg)) {
				return null;
			}

			List<String> arenas = plugin.getArenaRegistry().getArenas().stream().map(Arena::getId).collect(Collectors.toList());
			StringUtil.copyPartialMatches(args[1], arenas, completions);

			arenas.sort(null);
			return arenas;
		}

		completions.sort(null);
		return completions;
	}
}