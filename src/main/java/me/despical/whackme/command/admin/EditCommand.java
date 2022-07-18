package me.despical.whackme.command.admin;

import me.despical.whackme.arena.Arena;
import me.despical.whackme.arena.ArenaRegistry;
import me.despical.whackme.command.SubCommand;
import me.despical.whackme.handler.setup.SetupInventory;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author Despical
 * <p>
 * Created at 20.06.2022
 */
public class EditCommand extends SubCommand {

	public EditCommand() {
		super ("edit");

		setPermission("wm.admin.edit");
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

		new SetupInventory(arena, player).openInventory();
	}

	@Override
	public String getTutorial() {
		return "Opens the arena editor";
	}

	@Override
	public int getType() {
		return GENERIC;
	}

	@Override
	public int getSenderType() {
		return PLAYER;
	}
}