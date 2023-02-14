package me.despical.whackme.command.player;

import me.despical.whackme.arena.Arena;
import me.despical.whackme.arena.ArenaRegistry;
import me.despical.whackme.command.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author Despical
 * <p>
 * Created at 14.02.2023
 */
public class LeaveCommand extends SubCommand {

	public LeaveCommand() {
		super ("leave");
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
		final Player player = (Player) sender;
		final Arena arena = ArenaRegistry.getArena(player);

		if (arena == null) {
			player.sendMessage(chatManager.prefixedMessage("commands.not_playing"));
			return;
		}

		arena.removePlayer();
	}

	@Override
	public String getTutorial() {
		return null;
	}

	@Override
	public int getType() {
		return HIDDEN;
	}

	@Override
	public int getSenderType() {
		return PLAYER;
	}
}
