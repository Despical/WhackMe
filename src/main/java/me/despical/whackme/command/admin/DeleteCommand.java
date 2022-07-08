package me.despical.whackme.command.admin;

import me.despical.commons.configuration.ConfigUtils;
import me.despical.whackme.arena.Arena;
import me.despical.whackme.arena.ArenaRegistry;
import me.despical.whackme.command.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Despical
 * <p>
 * Created at 20.06.2022
 */
public class DeleteCommand extends SubCommand {

	private final Set<CommandSender> confirmations;

	public DeleteCommand() {
		super("delete");
		this.confirmations = new HashSet<>();

		setPermission("wm.admin.delete");
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
		Arena arena = ArenaRegistry.getArena(args[0]);

		if (arena == null) {
			sender.sendMessage(chatManager.prefixedMessage("commands.no_arena_like_that"));
			return;
		}

		if (!confirmations.contains(sender)) {
			confirmations.add(sender);
			plugin.getServer().getScheduler().runTaskLater(plugin, () -> confirmations.remove(sender), 200);
			sender.sendMessage(chatManager.prefixedMessage("commands.are_you_sure"));
			return;
		}

		confirmations.remove(sender);

		Player player = arena.getPlayer();

		if (player != null) {
			player.setFlySpeed(.1F);
			player.setWalkSpeed(.2F);

			arena.removePlayer();
			arena.teleportToEndLocation();
		}

		ArenaRegistry.unregisterArena(arena);

		config.set("instances." + args[0], null);
		ConfigUtils.saveConfig(plugin, config, "arenas");

		sender.sendMessage(chatManager.prefixedMessage("commands.removed_game_instance"));
	}

	@Override
	public String getTutorial() {
		return "Deletes arena with the current configuration";
	}

	@Override
	public CommandType getType() {
		return CommandType.GENERIC;
	}

	@Override
	public SenderType getSenderType() {
		return SenderType.BOTH;
	}
}