package me.despical.whackme.commands;

import me.despical.commandframework.Command;
import me.despical.commandframework.CommandArguments;
import me.despical.commons.string.StringMatcher;
import me.despical.commons.string.StringUtils;
import me.despical.whackme.ConfigPreferences;
import me.despical.whackme.WhackMe;
import me.despical.whackme.api.StatsStorage;
import me.despical.whackme.utils.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;
import java.util.stream.Collectors;

import static me.despical.commandframework.Command.SenderType.PLAYER;

public class PlayerCommands extends AbstractCommand {

	public PlayerCommands(WhackMe plugin) {
		super(plugin);

		plugin.getCommandFramework().setMatchFunction(arguments -> {
			if (arguments.isArgumentsEmpty()) return false;

			String label = arguments.getLabel(), arg = arguments.getArgument(0);

			final var matches = StringMatcher.match(arg, plugin.getCommandFramework().getCommands().stream().map(cmd -> cmd.name().replace(label + ".", "")).collect(Collectors.toList()));

			if (!matches.isEmpty()) {
				arguments.sendMessage(chatManager.prefixedMessage("commands.did_you_mean").replace("%command%", label + " " + matches.get(0).getMatch()));
				return true;
			}

			return false;
		});
	}

	@Command(
		name = "wm",
		usage = "/wm help",
		desc = "Main command of Whack Me plugin."
	)
	public void mainCommand(CommandArguments arguments) {
		arguments.sendMessage(chatManager.coloredRawMessage("&3This server is running &bWhack Me " + plugin.getDescription().getVersion() + " &3by &bDespical&3!"));

		if (arguments.hasPermission("wm.admin")) {
			arguments.sendMessage(chatManager.coloredRawMessage("&3Commands: &b/" + arguments.getLabel() + " help"));
		}
	}

	@Command(
		name = "wm.join",
		senderType = PLAYER
	)
	public void joinCommand(CommandArguments arguments) {
		if (arguments.isArgumentsEmpty()) {
			arguments.sendMessage(chatManager.prefixedMessage("commands.type_arena_name"));
			return;
		}

		final var arena = plugin.getArenaRegistry().getArena(arguments.getArgument(0));

		if (arena == null) {
			arguments.sendMessage(chatManager.prefixedMessage("commands.no_arena_like_that"));
			return;
		}

		final Player player = arguments.getSender();

		if (arena.containPlayer(player)) {
			player.sendMessage(chatManager.prefixedMessage("in_game.already_playing"));
			return;
		}

		if (arena.getPlayer() != null) {
			player.sendMessage(chatManager.prefixedMessage("in_game.someone_is_already_playing"));
			return;
		}

		if (!Utils.hasJoinPermission(player)) {
			player.sendMessage(chatManager.prefixedMessage("commands.no_permission"));
			return;
		}

		arena.addPlayer(player);
	}

	@Command(
		name = "wm.leave",
		senderType = PLAYER
	)
	public void leaveCommand(CommandArguments arguments) {
		final Player player = arguments.getSender();
		final var arena = plugin.getArenaRegistry().getArena(player);

		if (arena == null) {
			player.sendMessage(chatManager.prefixedMessage("commands.not_playing"));
			return;
		}

		arena.removePlayer();
	}

	@Command(
		name = "wm.randomjoin",
		senderType = PLAYER
	)
	public void randomJoinCommand(CommandArguments arguments) {
		final Player player = arguments.getSender();

		if (plugin.getOption(ConfigPreferences.Option.BLOCK_LEAVE_COMMAND)) return;

		if (plugin.getArenaRegistry().isInArena(player)) {
			player.sendMessage(chatManager.prefixedMessage("in_game.already_playing"));
			return;
		}

		final var arenas = plugin.getArenaRegistry().getArenas().stream().filter(arena -> arena.getPlayer() == null && arena.isReady()).toList();

		if (!arenas.isEmpty()) {
			var arena = arenas.get(0);

			if (!Utils.hasJoinPermission(player)) {
				player.sendMessage(chatManager.prefixedMessage("commands.no_permission"));
				return;
			}

			arena.addPlayer(player);
			return;
		}

		player.sendMessage(plugin.getChatManager().prefixedMessage("commands.no_free_arenas"));
	}

	@Command(
		name = "wm.stats",
		senderType = PLAYER
	)
	public void statsCommand(CommandArguments argument) {
		final Player player = argument.getSender(), target = argument.isArgumentsEmpty() ? player : plugin.getServer().getPlayer(argument.getArgument(0));

		if (target == null) {
			player.sendMessage(chatManager.prefixedMessage("commands.player_not_found"));
			return;
		}

		final var user = plugin.getUserManager().getUser(target);
		final var path = "commands.stats_command.";

		if (player.equals(target)) {
			player.sendMessage(chatManager.message(path + "header", player));
		} else {
			player.sendMessage(chatManager.message(path + "header_other", target));
		}

		player.sendMessage(chatManager.message(path + "tours_played", player) + user.getStat(StatsStorage.StatisticType.TOURS_PLAYED));
		player.sendMessage(chatManager.message(path + "record_score", player) + user.getStat(StatsStorage.StatisticType.RECORD_SCORE));
		player.sendMessage(chatManager.message(path + "footer", player));
	}

	@Command(
		name = "wm.top",
		senderType = PLAYER
	)
	public void leaderboardCommand(CommandArguments arguments) {
		if (arguments.isArgumentsEmpty()) {
			arguments.sendMessage(chatManager.prefixedMessage("commands.statistics.type_name"));
			return;
		}

		try {
			printLeaderboard(arguments.getSender(), StatsStorage.StatisticType.valueOf(arguments.getArgument(0).toUpperCase(java.util.Locale.ENGLISH)));
		} catch (IllegalArgumentException exception) {
			arguments.sendMessage(chatManager.prefixedMessage("commands.statistics.invalid_name"));
		}
	}

	private void printLeaderboard(CommandSender sender, StatsStorage.StatisticType statisticType) {
		sender.sendMessage(plugin.getChatManager().message("commands.statistics.header"));

		final var stats = StatsStorage.getStats(statisticType);
		final var statistic = StringUtils.capitalize(statisticType.name().toLowerCase(java.util.Locale.ENGLISH).replace("_", " "));

		for (var i = 0; i < 10; i++) {
			try {
				var current = (UUID) stats.keySet().toArray()[stats.keySet().toArray().length - 1];
				sender.sendMessage(formatMessage(statistic, plugin.getServer().getOfflinePlayer(current).getName(), i + 1, stats.get(current)));
				stats.remove(current);
			} catch (IndexOutOfBoundsException ex) {
				sender.sendMessage(formatMessage(statistic, "Empty", i + 1, 0));
			} catch (NullPointerException ex) {
				var current = (UUID) stats.keySet().toArray()[stats.keySet().toArray().length - 1];

				if (plugin.getOption(ConfigPreferences.Option.DATABASE_ENABLED)) {
					try (Connection connection = plugin.getMysqlDatabase().getConnection()) {
						var statement = connection.createStatement();
						var set = statement.executeQuery("SELECT name FROM playerstats WHERE UUID='" + current.toString() + "'");

						if (set.next()) {
							sender.sendMessage(formatMessage(statistic, set.getString(1), i + 1, stats.get(current)));
							continue;
						}
					} catch (SQLException ignored) {}
				}

				sender.sendMessage(formatMessage(statistic, chatManager.message("commands.statistics.unknown_player"), i + 1, stats.get(current)));
			}
		}
	}

	private String formatMessage(String statisticName, String playerName, int position, int value) {
		var message = chatManager.message("commands.statistics.format");

		message = message.replace("%position%", Integer.toString(position));
		message = message.replace("%name%", playerName);
		message = message.replace("%value%", Integer.toString(value));
		message = message.replace("%statistic%", statisticName);
		return message;
	}
}