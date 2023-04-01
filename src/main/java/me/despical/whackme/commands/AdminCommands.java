package me.despical.whackme.commands;

import me.despical.commandframework.Command;
import me.despical.commandframework.CommandArguments;
import me.despical.commons.configuration.ConfigUtils;
import me.despical.commons.miscellaneous.MiscUtils;
import me.despical.commons.serializer.LocationSerializer;
import me.despical.commons.util.LogUtils;
import me.despical.whackme.Main;
import me.despical.whackme.arena.Arena;
import me.despical.whackme.handlers.setup.SetupInventory;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static me.despical.commandframework.Command.SenderType.PLAYER;

public class AdminCommands extends AbstractCommand {

	private final Set<CommandSender> confirmations;

	public AdminCommands(Main plugin) {
		super(plugin);
		this.confirmations = new HashSet<>();
	}

	@Command(
		name = "wm.create",
		permission = "wm.admin.create",
		usage = "/wm create <id>",
		desc = "Creates a new arena with default configuration",
		senderType = PLAYER
	)
	public void createCommand(CommandArguments arguments) {
		final Player player = arguments.getSender();

		if (arguments.isArgumentsEmpty()) {
			player.sendMessage(chatManager.prefixedRawMessage("&cPlease enter an name to create an arena!"));
			return;
		}

		final String id = arguments.getArgument(0);

		if (plugin.getArenaRegistry().isArena(id)) {
			player.sendMessage(chatManager.prefixedRawMessage("&cArena with that ID already contains!"));
			player.sendMessage(chatManager.prefixedRawMessage("&cTo check existing arenas use: /wm list"));
			return;
		}

		player.sendMessage(chatManager.coloredRawMessage("&l--------------------------------------------"));
		MiscUtils.sendCenteredMessage(player, "&eInstance &a&l" + id + " &ecreated!");
		player.sendMessage("");
		MiscUtils.sendCenteredMessage(player, "&aEdit this arena via /wm edit &6" + id + "&a!");
		player.sendMessage("");
		MiscUtils.sendCenteredMessage(player, "&6Don't know where to start? Check out our wiki:");
		MiscUtils.sendCenteredMessage(player, "&7https://www.github.com/Despical/WhackMe/wiki");
		player.sendMessage(chatManager.coloredRawMessage("&l--------------------------------------------"));

		final String path = "instances." + id + ".";

		arenaConfig.set(path + "ready", false);
		arenaConfig.set(path + "endLocation", LocationSerializer.SERIALIZED_LOCATION);
		arenaConfig.set(path + "centerLocation", LocationSerializer.SERIALIZED_LOCATION);

		ConfigUtils.saveConfig(plugin, arenaConfig, "arenas");

		Arena arena = new Arena(id);
		arena.setReady(false);
		arena.setEndLocation(LocationSerializer.DEFAULT_LOCATION);
		arena.setStartLocation(LocationSerializer.DEFAULT_LOCATION);

		plugin.getArenaRegistry().registerArena(arena);
	}

	@Command(
		name = "wm.delete",
		permission = "wm.admin.delete",
		usage = "/wm delete <arena>",
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

		arenaConfig.set("instances." + arenaId, null);
		ConfigUtils.saveConfig(plugin, arenaConfig, "arenas");

		sender.sendMessage(chatManager.prefixedMessage("commands.removed_game_instance"));
	}

	@Command(
		name = "wm.edit",
		permission = "wm.admin.edit",
		usage = "/wm edit <arena>",
		desc = "Opens the arena editor",
		min = 1,
		senderType = PLAYER
	)
	public void editCommand(CommandArguments arguments) {
		final Player player = arguments.getSender();
		final Arena arena = plugin.getArenaRegistry().getArena(arguments.getArgument(0));

		if (arena == null) {
			player.sendMessage(chatManager.prefixedMessage("commands.no_arena_like_that"));
			return;
		}

		new SetupInventory(arena, player).openInventory();
	}
	@Command(
		name = "wm.help",
		permission = "wm.admin.help"
	)
	public void helpCommand(CommandArguments arguments) {
		arguments.sendMessage("");
		arguments.sendMessage(chatManager.coloredRawMessage("&3&l---- Whack Me Admin Commands ----"));
		arguments.sendMessage("");

		final CommandSender sender = arguments.getSender();
		final boolean isPlayer = arguments.isSenderPlayer();

		for (final Command command : plugin.getCommandFramework().getCommands()) {
			final String usage = command.usage(), desc = command.desc();

			if (usage.isEmpty()) continue;

			if (isPlayer) {
				((Player) sender).spigot().sendMessage(new ComponentBuilder(usage)
					.color(ChatColor.AQUA)
					.event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, usage))
					.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(desc)))
					.create());
			} else {
				sender.sendMessage(chatManager.coloredRawMessage("&b" + usage + " &3- &b" + desc));
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
		desc = "Kicks specified player if they're playing",
		min = 1
	)
	public void reloadCommand(CommandArguments arguments) {
		LogUtils.log("Initialized plugin reload by {0}.", arguments.getSender().getName());

		final long start = System.currentTimeMillis();

		plugin.reloadConfig();
		plugin.getChatManager().reloadConfig();
		plugin.getConfigPreferences().reload();

		for (Arena arena : plugin.getArenaRegistry().getArenas()) {
			LogUtils.log("Stopping arena called {0}.", arena.getId());

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

		LogUtils.log("Finished reloading took {0} ms", System.currentTimeMillis() - start);
	}
}