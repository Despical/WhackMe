package me.despical.whackme.command;

import me.despical.commandframework.CommandArguments;
import me.despical.commandframework.CommandFramework;
import me.despical.commandframework.annotations.Command;
import me.despical.commandframework.annotations.Param;
import me.despical.commons.string.StringMatcher;
import me.despical.commons.string.StringUtils;
import me.despical.whackme.ConfigPreferences;
import me.despical.whackme.api.statistics.StatisticType;
import me.despical.whackme.api.statistics.StatsStorage;
import me.despical.whackme.arena.Arena;
import me.despical.whackme.user.User;
import me.despical.whackme.user.data.MySQLManager;
import me.despical.whackme.utils.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;

import static me.despical.whackme.api.statistics.StatisticType.*;

public class PlayerCommands extends AbstractCommandHandler {

	@Command(
		name = "wm",
		usage = "/wm",
		desc = "Main command of the plugin."
	)
	public void mainCommand(CommandArguments arguments) {
		if (arguments.isArgumentsEmpty()) {
			arguments.sendMessage("&3This server is running &bWhack Me {0} &3by &bDespical&3.", plugin.getDescription().getVersion());

			if (arguments.hasPermission("wm.admin")) {
				arguments.sendMessage("&3Commands: &b/" + arguments.getLabel() + " help");
			}

			return;
		}

		CommandFramework commandFramework = plugin.getCommandFramework();
		String label = arguments.getLabel(), arg = arguments.getArgument(0);
		List<String> commands = commandFramework.getSubCommands().stream().map(cmd -> cmd.name().replace(label + ".", "")).collect(Collectors.toList());
		List<StringMatcher.Match> matches = StringMatcher.match(arg, commands);

		if (!matches.isEmpty()) {
			Optional<Command> optionalMatch = commandFramework.getSubCommands().stream().filter(cmd -> cmd.name().equals(label + "." + matches.get(0).getMatch())).findFirst();

			if (optionalMatch.isPresent()) {
				String matchedName = getMatchingParts(optionalMatch.get().name(), label + "." + String.join(".", arguments.getArguments()));
				Optional<Command> matchedCommand = commandFramework.getSubCommands().stream().filter(cmd -> cmd.name().equals(matchedName)).findFirst();

				if (matchedCommand.isPresent()) {
					arguments.sendMessage(chatManager.prefixedMessage("commands.correct_usage").replace("%usage%", matchedCommand.get().usage()));
					return;
				}

				arguments.sendMessage(chatManager.prefixedMessage("commands.did_you_mean").replace("%command%", optionalMatch.get().usage()));
				return;
			}

			arguments.sendMessage(chatManager.prefixedMessage("commands.did_you_mean").replace("%command%", "/" + label));
		}
	}

	@Command(
		name = "wm.join",
		usage = "/wm join <arena>",
		senderType = Command.SenderType.PLAYER
	)
	public void joinCommand(Arena arena, CommandArguments arguments) {
		if (arguments.isArgumentsEmpty()) {
			arguments.sendMessage(chatManager.prefixedMessage("commands.type_arena_name"));
			return;
		}

		if (arena == null) {
			arguments.sendMessage(chatManager.prefixedMessage("commands.no_arena_like_that"));
			return;
		}

		plugin.getArenaManager().joinAttempt(arguments.getSender(), arena);
	}

	@Command(
		name = "wm.leave",
		usage = "/wm leave",
		senderType = Command.SenderType.PLAYER
	)
	public void leaveCommand(Player player, @Param("pArena") Arena arena, CommandArguments arguments) {
		if (arena == null) {
			player.sendMessage(chatManager.prefixedMessage("commands.not_playing"));
			return;
		}

		arena.removePlayer();
	}

	@Command(
		name = "wm.randomjoin",
		usage = "/wm randomjoin",
		senderType = Command.SenderType.PLAYER
	)
	public void randomJoinCommand(Player player, CommandArguments arguments) {
		if (plugin.getArenaRegistry().isInArena(player)) {
			player.sendMessage(chatManager.prefixedMessage("in_game.already_playing"));
			return;
		}

		List<Arena> arenas = plugin.getArenaRegistry().getArenas()
			.stream()
			.filter(arena -> arena.getPlayer() == null && arena.isReady())
			.sorted()
			.collect(Collectors.toList());

		if (!arenas.isEmpty()) {
			Arena arena = arenas.get(0);

			if (!Utils.hasJoinPermission(player)) {
				player.sendMessage(chatManager.prefixedMessage("commands.no_permission"));
				return;
			}

			plugin.getArenaManager().joinAttempt(player, arena);
			return;
		}

		player.sendMessage(chatManager.prefixedMessage("commands.no_free_arenas"));
	}

	@Command(
		name = "wm.stats",
		usage = "/wm stats [player]",
		senderType = Command.SenderType.PLAYER
	)
	public void statsCommand(Player player, CommandArguments argument) {
		final Player target = argument.isArgumentsEmpty() ? player : plugin.getServer().getPlayer(argument.getArgument(0));

		if (target == null) {
			player.sendMessage(chatManager.prefixedMessage("commands.player_not_found"));
			return;
		}

		final User user = plugin.getUserManager().getUser(target);

		chatManager.getStringList("commands.stats_command.messages")
			.stream()
			.map(message -> formatStats(message, player.equals(target) ? "header" : "header_other", user))
			.forEach(player::sendMessage);
	}

	private String formatStats(String message, String header, User user) {
		final int minusBlocks = user.getStat(MINUS_BLOCKS), plusBlocks = user.getStat(PLUS_BLOCKS);

		message = message.replace("%player%", user.getName());
		message = message.replace("%header%", chatManager.message("commands.stats_command." + header, user.getPlayer()));
		message = message.replace("%tours_played%", TOURS_PLAYED.from(user));
		message = message.replace("%record_score%", RECORD_SCORE.from(user));
		message = message.replace("%whacked_point_blocks%", Integer.toString(plusBlocks));
		message = message.replace("%whacked_minus_point_blocks%", Integer.toString(minusBlocks));
		message = message.replace("%whacked_block_rate%", String.format("%.1f", (minusBlocks + plusBlocks == 0 ? 100 : ((double) plusBlocks / (minusBlocks + plusBlocks)) * 100D)));
		message = message.replace("%longest_point_streak%", LONGEST_STREAK.from(user));
		return chatManager.coloredRawMessage(message);
	}

	@Command(
		name = "wm.top",
		usage = "/wm top <statistic>",
		senderType = Command.SenderType.PLAYER
	)
	public void leaderboardCommand(CommandArguments arguments) {
		if (arguments.isArgumentsEmpty()) {
			arguments.sendMessage(chatManager.prefixedMessage("commands.statistics.type_name"));
			return;
		}

		try {
			printLeaderboard(arguments.getSender(), StatisticType.valueOf(arguments.getArgument(0).toUpperCase(java.util.Locale.ENGLISH)));
		} catch (IllegalArgumentException exception) {
			arguments.sendMessage(chatManager.prefixedMessage("commands.statistics.invalid_name"));
		}
	}

	private void printLeaderboard(CommandSender sender, StatisticType statisticType) {
		sender.sendMessage(chatManager.message("commands.statistics.header"));

		final Map<UUID, Integer> stats = StatsStorage.getStats(statisticType);
		final String statistic = StringUtils.capitalize(statisticType.name().toLowerCase(java.util.Locale.ENGLISH).replace("_", " "));

		for (int i = 0; i < 10; i++) {
			try {
				UUID current = (UUID) stats.keySet().toArray()[stats.keySet().toArray().length - 1];
				sender.sendMessage(formatMessage(statistic, plugin.getServer().getOfflinePlayer(current).getName(), i + 1, stats.get(current)));
				stats.remove(current);
			} catch (IndexOutOfBoundsException ex) {
				sender.sendMessage(formatMessage(statistic, "Empty", i + 1, 0));
			} catch (NullPointerException ex) {
				UUID current = (UUID) stats.keySet().toArray()[stats.keySet().toArray().length - 1];

				if (plugin.getOption(ConfigPreferences.Option.DATABASE_ENABLED)) {
					MySQLManager mysqlManager = (MySQLManager) plugin.getUserManager().getUserDatabase();
					String table = mysqlManager.getTableName();

					try (Connection connection = mysqlManager.getDatabase().getConnection()) {
						Statement statement = connection.createStatement();
						ResultSet set = statement.executeQuery(String.format("SELECT name FROM %s WHERE UUID='%s'", table, current.toString()));

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
		String message = chatManager.message("commands.statistics.format");

		message = message.replace("%position%", Integer.toString(position));
		message = message.replace("%name%", playerName);
		message = message.replace("%value%", Integer.toString(value));
		message = message.replace("%statistic%", statisticName);
		return message;
	}

	private String getMatchingParts(String matched, String current) {
		String[] matchedArray = matched.split("\\."), currentArray = current.split("\\.");
		int max = Math.min(matchedArray.length, currentArray.length);
		List<String> matchingParts = new ArrayList<>();

		for (int i = 0; i < max; i++) {
			if (matchedArray[i].equals(currentArray[i])) {
				matchingParts.add(matchedArray[i]);
			}
		}

		return String.join(".", matchingParts);
	}
}