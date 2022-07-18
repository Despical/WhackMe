package me.despical.whackme.command;

import me.despical.commons.string.StringMatcher;
import me.despical.whackme.Main;
import me.despical.whackme.command.admin.*;
import me.despical.whackme.command.player.JoinCommand;
import me.despical.whackme.command.player.LeaderboardCommand;
import me.despical.whackme.command.player.StatsCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Despical
 * <p>
 * Created at 18.06.2022
 */
public class CommandHandler implements CommandExecutor {

	private final Main plugin;
	private final Set<SubCommand> subCommands;

	public CommandHandler(Main plugin) {
		this.plugin = plugin;
		this.subCommands = new HashSet<>();

		SubCommand[] commands = {new CreateCommand(), new DeleteCommand(), new ListCommand(), new EditCommand(), new HelpCommand(), new ReloadCommand(), new StatsCommand(), new LeaderboardCommand(), new JoinCommand()};

		for (SubCommand command : commands) {
			registerSubCommand(command);
		}

		Optional.ofNullable(plugin.getCommand("whackme")).ifPresent(command -> {
			command.setExecutor(this);
			command.setTabCompleter(new TabCompletion(plugin));
		});
	}

	public void registerSubCommand(SubCommand subCommand) {
		subCommands.add(subCommand);
	}

	public Set<SubCommand> getSubCommands() {
		return new HashSet<>(subCommands);
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
		if (args.length == 0) {
			sender.sendMessage(plugin.getChatManager().coloredRawMessage("&3This server is running &bWhack Me v" + plugin.getDescription().getVersion() + " &3by&b Despical"));

			if (sender.hasPermission("wm.admin")) {
				sender.sendMessage(plugin.getChatManager().coloredRawMessage("&3Commands: &b/" + label + " help"));
				sender.sendMessage(plugin.getChatManager().coloredRawMessage("&3Thank you for &bpurchasing &3our plugin and &bsupporting &3us! <3"));
			}

			return true;
		}

		for (SubCommand subCommand : subCommands) {
			if (subCommand.getName().equalsIgnoreCase(args[0])) {
				if (!subCommand.hasPermission(sender)) {
					sender.sendMessage(subCommand.chatManager.prefixedMessage("commands.no_permission"));
					return true;
				}

				if (subCommand.getSenderType() == 1 && !(sender instanceof Player)) {
					sender.sendMessage(subCommand.chatManager.prefixedMessage("commands.only_by_player"));
					return true;
				}

				if (args.length - 1 >= subCommand.getMinimumArguments()) {
					try {
						subCommand.execute(sender, label, Arrays.copyOfRange(args, 1, args.length));
					} catch (CommandException exception) {
						sender.sendMessage(subCommand.chatManager.coloredRawMessage("&c" + exception.getMessage()));
					}
				} else if (subCommand.getType() == 0) {
					sender.sendMessage(subCommand.chatManager.coloredRawMessage("&cUsage: /" + label + " " + subCommand.getName() + " " + (subCommand.getPossibleArguments().length() > 0 ? subCommand.getPossibleArguments() : "")));
				}

				return true;
			}
		}

		final List<StringMatcher.Match> matches = StringMatcher.match(args[0], subCommands.stream().map(SubCommand::getName).collect(Collectors.toList()));

		if (!matches.isEmpty()) {
			sender.sendMessage(plugin.getChatManager().prefixedMessage("commands.did_you_mean").replace("%command%", label + " " + matches.get(0).getMatch()));
			return true;
		}

		return true;
	}
}