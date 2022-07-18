package me.despical.whackme.command.admin;

import me.despical.whackme.arena.Arena;
import me.despical.whackme.arena.ArenaRegistry;
import me.despical.whackme.command.SubCommand;
import org.bukkit.command.CommandSender;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Despical
 * <p>
 * Created at 20.06.2022
 */
public class ListCommand extends SubCommand {

	public ListCommand() {
		super ("list");

		setPermission("wm.admin.list");
	}

	@Override
	public String getPossibleArguments() {
		return null;
	}

	@Override
	public int getMinimumArguments() {
		return 0;
	}

	@Override
	public void execute(CommandSender sender, String label, String[] args) {
		final Set<Arena> arenas = ArenaRegistry.getArenas();

		if (arenas.isEmpty()) {
			sender.sendMessage(chatManager.prefixedMessage("commands.list_command.no_arenas_created"));
			return;
		}

		sender.sendMessage(chatManager.prefixedMessage("commands.list_command.format").replace("%list%", arenas.stream().map(Arena::getId).collect(Collectors.joining(", "))));
	}

	@Override
	public String getTutorial() {
		return "Shows all of the existing arenas";
	}

	@Override
	public int getType() {
		return GENERIC;
	}

	@Override
	public int getSenderType() {
		return BOTH;
	}
}