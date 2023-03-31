package me.despical.whackme.commands;

import me.despical.commandframework.Command;
import me.despical.commandframework.CommandArguments;
import me.despical.commons.string.StringMatcher;
import me.despical.whackme.ConfigPreferences;
import me.despical.whackme.Main;
import me.despical.whackme.api.StatsStorage;
import me.despical.whackme.arena.Arena;
import me.despical.whackme.user.User;
import me.despical.whackme.user.data.MysqlManager;
import me.despical.whackme.utils.Utils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static me.despical.commandframework.Command.SenderType.PLAYER;

public class PlayerCommands extends AbstractCommand {

	public PlayerCommands(Main plugin) {
		super(plugin);

		plugin.getCommandFramework().setAnyMatch(arguments -> {
			if (arguments.isArgumentsEmpty()) return;

			String label = arguments.getLabel(), arg = arguments.getArgument(0);

			List<StringMatcher.Match> matches = StringMatcher.match(arg, plugin.getCommandFramework().getCommands().stream().map(cmd -> cmd.name().replace(label + ".", "")).collect(Collectors.toList()));

			if (!matches.isEmpty()) {
				arguments.sendMessage(chatManager.prefixedMessage("commands.did_you_mean").replace("%command%", label + " " + matches.get(0).getMatch()));
			}
		});
	}

	@Command(
		name = "wm"
	)
	public void mainCommand(CommandArguments arguments) {
		if (arguments.isArgumentsEmpty()) {
			arguments.sendMessage(chatManager.coloredRawMessage("&3This server is running &bWhack Me &3v" + plugin.getDescription().getVersion() + " by &bDespical"));

			if (arguments.hasPermission("wm.admin")) {
				arguments.sendMessage(chatManager.coloredRawMessage("&3Commands: &b/" + arguments.getLabel() + " help"));
			}
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

		final Arena arena = plugin.getArenaRegistry().getArena(arguments.getArgument(0));

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
		final Arena arena = plugin.getArenaRegistry().getArena(player);

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

		if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.BLOCK_LEAVE_COMMAND)) return;

		if (plugin.getArenaRegistry().isInArena(player)) {
			player.sendMessage(chatManager.prefixedMessage("in_game.already_playing"));
			return;
		}

		final List<Arena> arenas = plugin.getArenaRegistry().getArenas().stream().filter(arena -> arena.getPlayer() == null).collect(Collectors.toList());

		if (!arenas.isEmpty()) {
			Arena arena = arenas.get(0);

			if (!Utils.hasJoinPermission(player)) {
				player.sendMessage(chatManager.prefixedMessage("commands.no_permission"));
				return;
			}

			arena.addPlayer(player);
			return;
		}

		player.sendMessage(plugin.getChatManager().message("commands.no_free_arenas"));
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

		final User user = plugin.getUserManager().getUser(target);
		final String path = "commands.stats_command.";

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
		final Map<UUID, Integer> stats = StatsStorage.getStats(statisticType);
		sender.sendMessage(plugin.getChatManager().message("commands.statistics.header"));

		final String statistic = StringUtils.capitalize(statisticType.name().toLowerCase(java.util.Locale.ENGLISH).replace("_", " "));

		for (int i = 0; i < 10; i++) {
			try {
				final UUID current = (UUID) stats.keySet().toArray()[stats.keySet().toArray().length - 1];
				sender.sendMessage(formatMessage(statistic, Bukkit.getOfflinePlayer(current).getName(), i + 1, stats.get(current)));
				stats.remove(current);
			} catch (IndexOutOfBoundsException ex) {
				sender.sendMessage(formatMessage(statistic, "Empty", i + 1, 0));
			} catch (NullPointerException ex) {
				UUID current = (UUID) stats.keySet().toArray()[stats.keySet().toArray().length - 1];

				if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.DATABASE_ENABLED)) {
					try (Connection connection = plugin.getMysqlDatabase().getConnection()) {
						final Statement statement = connection.createStatement();
						final ResultSet set = statement.executeQuery("SELECT name FROM " + ((MysqlManager) plugin.getUserManager().getDatabase()).getTableName() + " WHERE UUID='" + current.toString() + "'");

						if (set.next()) {
							sender.sendMessage(formatMessage(statistic, set.getString(1), i + 1, stats.get(current)));
							continue;
						}
					} catch (SQLException ignored) {}
				}

				sender.sendMessage(formatMessage(statistic, "Unknown Player", i + 1, stats.get(current)));
			}
		}
	}

	private String formatMessage(String statisticName, String playerName, int position, int value) {
		String message = chatManager.message("commands.statistics.format");

		message = StringUtils.replace(message, "%position%", Integer.toString(position));
		message = StringUtils.replace(message, "%name%", playerName);
		message = StringUtils.replace(message, "%value%", Integer.toString(value));
		message = StringUtils.replace(message, "%statistic%", statisticName);
		return message;
	}
}