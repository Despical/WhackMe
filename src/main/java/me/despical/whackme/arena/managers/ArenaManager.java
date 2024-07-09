package me.despical.whackme.arena.managers;

import me.despical.whackme.WhackMe;
import me.despical.whackme.arena.Arena;
import me.despical.whackme.user.User;
import me.despical.whackme.utils.Utils;
import org.bukkit.entity.Player;

/**
 * @author Despical
 * <p>
 * Created at 31.01.2024
 */
public class ArenaManager {

	private final WhackMe plugin;

	public ArenaManager(WhackMe plugin) {
		this.plugin = plugin;
	}

	public void joinAttempt(Player player, Arena arena) {
		if (!arena.isReady()) {
			player.sendMessage(plugin.getChatManager().prefixedMessage("commands.arena_not_configured"));
			return;
		}

		if (arena.containPlayer(player)) {
			player.sendMessage(plugin.getChatManager().prefixedMessage("in_game.already_playing"));
			return;
		}

		if (arena.getPlayer() != null) {
			player.sendMessage(plugin.getChatManager().prefixedMessage("in_game.someone_is_already_playing"));
			return;
		}

		if (!Utils.hasJoinPermission(player)) {
			player.sendMessage(plugin.getChatManager().prefixedMessage("commands.no_permission"));
			return;
		}

		User user = plugin.getUserManager().getUser(player);

		if (user.getCooldown("play_again") > 0) {
			player.sendMessage(plugin.getChatManager().prefixedMessage("commands.wait_for_cooldown"));
			return;
		}

		arena.addPlayer(player);
	}
}