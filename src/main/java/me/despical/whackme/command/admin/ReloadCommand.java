package me.despical.whackme.command.admin;

import me.despical.commons.util.LogUtils;
import me.despical.whackme.arena.Arena;
import me.despical.whackme.arena.ArenaRegistry;
import me.despical.whackme.command.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author Despical
 * <p>
 * Created at 20.06.2022
 */
public class ReloadCommand extends SubCommand {

	public ReloadCommand() {
		super ("reload");

		setPermission("wm.admin.reload");
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
		LogUtils.log("Initialized plugin reload by {0}.", sender.getName());

		final long start = System.currentTimeMillis();

		plugin.reloadConfig();
		plugin.getChatManager().reloadConfig();

		for (Arena arena : ArenaRegistry.getArenas()) {
			LogUtils.log("Stopping arena called {0}.", arena.getId());

			final Player player = arena.getPlayer();

			if (player != null) {
				player.setFlySpeed(.1F);
				player.setWalkSpeed(.2F);

				arena.removePlayer();
				arena.teleportToEndLocation();
			}
		}

		ArenaRegistry.registerArenas();
		sender.sendMessage(chatManager.prefixedMessage("commands.success_reload"));

		LogUtils.log("Finished reloading took {0} ms", System.currentTimeMillis() - start);
	}

	@Override
	public String getTutorial() {
		return "Reloads all of the system configuration and arenas";
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