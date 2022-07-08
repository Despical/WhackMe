package me.despical.whackme.command.player;

import me.despical.whackme.api.StatsStorage;
import me.despical.whackme.command.SubCommand;
import me.despical.whackme.user.User;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author Despical
 * <p>
 * Created at 20.06.2022
 */
public class StatsCommand extends SubCommand {

	public StatsCommand() {
		super ("stats");
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
		Player player = (Player) sender, target = args.length == 0 ? player : plugin.getServer().getPlayer(args[0]);

		if (target == null) {
			player.sendMessage(chatManager.prefixedMessage("commands.player_not_found"));
			return;
		}

		User user = plugin.getUserManager().getUser(target);
		String path = "commands.stats_command.";

		if (player.equals(target)) {
			player.sendMessage(chatManager.message(path + "header", player));
		} else {
			player.sendMessage(chatManager.message(path + "header_other", target));
		}

		player.sendMessage(chatManager.message(path + "tours_played", player) + user.getStat(StatsStorage.StatisticType.TOURS_PLAYED));
		player.sendMessage(chatManager.message(path + "record_score", player) + user.getStat(StatsStorage.StatisticType.RECORD_SCORE));
		player.sendMessage(chatManager.message(path + "footer", player));
	}

	@Override
	public String getTutorial() {
		return null;
	}

	@Override
	public CommandType getType() {
		return CommandType.HIDDEN;
	}

	@Override
	public SenderType getSenderType() {
		return SenderType.PLAYER;
	}
}