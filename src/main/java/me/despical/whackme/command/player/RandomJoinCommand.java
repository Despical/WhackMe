package me.despical.whackme.command.player;

import me.despical.whackme.ConfigPreferences;
import me.despical.whackme.arena.Arena;
import me.despical.whackme.arena.ArenaRegistry;
import me.despical.whackme.command.SubCommand;
import me.despical.whackme.util.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Despical
 * <p>
 * Created at 14.02.2023
 */
public class RandomJoinCommand extends SubCommand {

	public RandomJoinCommand() {
		super ("randomjoin");
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

		if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.BLOCK_LEAVE_COMMAND)) return;

		if (ArenaRegistry.isInArena(player)) {
			player.sendMessage(chatManager.prefixedMessage("in_game.already_playing"));
			return;
		}

		final List<Arena> arenas = ArenaRegistry.getArenas().stream().filter(arena -> arena.getPlayer() == null).collect(Collectors.toList());

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