package me.despical.whackme.command;

import me.despical.commons.util.Collections;
import me.despical.whackme.Main;
import me.despical.whackme.arena.Arena;
import me.despical.whackme.arena.ArenaRegistry;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Despical
 * <p>
 * Created at 20.06.2022
 */
public class TabCompletion implements TabCompleter {

	private final Main plugin;

	public TabCompletion(Main plugin) {
		this.plugin = plugin;
	}

	@Override
	public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
		List<String> completions = new ArrayList<>(), commands = plugin.getCommandHandler().getSubCommands().stream().map(SubCommand::getName).collect(Collectors.toList());
		String arg = args[0];

		if (args.length == 1) {
			StringUtil.copyPartialMatches(arg, commands, completions);
		}

		if (args.length == 2) {
			if (arg.equalsIgnoreCase("top")) {
				return Collections.listOf("tours_played", "record_score");
			}

			if (arg.equalsIgnoreCase("stats")) {
				return plugin.getServer().getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
			}

			if (!commands.contains(arg)) {
				return null;
			}

			List<String> arenas = ArenaRegistry.getArenas().stream().map(Arena::getId).collect(Collectors.toList());
			StringUtil.copyPartialMatches(args[1], arenas, completions);

			arenas.sort(null);
			return arenas;
		}

		completions.sort(null);
		return completions;
	}
}