package me.despical.whackme.command.player;

import me.despical.whackme.arena.Arena;
import me.despical.whackme.arena.ArenaRegistry;
import me.despical.whackme.command.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author Despical
 * <p>
 * Created at 21.06.2022
 */
public class JoinCommand extends SubCommand {

	public JoinCommand() {
		super ("join");
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
		if (args.length == 0) {
			sender.sendMessage(chatManager.prefixedMessage("commands.type_arena_name"));
			return;
		}

		final Arena arena = ArenaRegistry.getArena(args[0]);

		if (arena == null) {
			sender.sendMessage(chatManager.prefixedMessage("commands.no_arena_like_that"));
			return;
		}

		final Player player = (Player) sender;

		if (arena.containPlayer(player)) {
			player.sendMessage(chatManager.prefixedMessage("in_game.already_playing"));
			return;
		}

		if (arena.getPlayer() != null) {
			player.sendMessage(chatManager.prefixedMessage("in_game.someone_is_already_playing"));
			return;
		}

		arena.addPlayer(player);
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
