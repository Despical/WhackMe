package me.despical.whackme.command.admin;

import me.despical.whackme.arena.Arena;
import me.despical.whackme.arena.ArenaRegistry;
import me.despical.whackme.command.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author Despical
 * <p>
 * Created at 13.03.2023
 */
public class KickCommand extends SubCommand {

	public KickCommand() {
		super ("kick");

		setPermission("wm.admin.kick");
	}

	@Override
	public String getPossibleArguments() {
		return "<arena>";
	}

	@Override
	public int getMinimumArguments() {
		return 1;
	}

	@Override
	public void execute(CommandSender sender, String label, String[] args) {
		final Player player = (Player) sender;
		final Arena arena = ArenaRegistry.getArena(args[0]);

		if (arena == null) {
			player.sendMessage(chatManager.prefixedMessage("commands.no_arena_like_that"));
			return;
		}

		arena.removePlayer();
	}

	@Override
	public String getTutorial() {
		return "Kicks player from the arena";
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