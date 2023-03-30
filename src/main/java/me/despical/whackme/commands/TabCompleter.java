package me.despical.whackme.commands;

import me.despical.commandframework.CommandArguments;
import me.despical.commandframework.Completer;
import me.despical.commons.util.Collections;
import me.despical.whackme.Main;
import me.despical.whackme.arena.Arena;
import me.despical.whackme.arena.ArenaRegistry;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Despical
 * <p>
 * Created at 20.06.2022
 */
public class TabCompleter extends AbstractCommand {

	public TabCompleter(Main plugin) {
		super(plugin);
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