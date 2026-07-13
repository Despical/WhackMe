package dev.despical.whackme.event;

import dev.despical.whackme.command.PlayingCommandPolicy;
import dev.despical.whackme.option.BooleanOption;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

/**
 * @author Despical
 * <p>
 * Created at 13.07.2026
 */
public final class CommandBlockEvents extends ListenerAdapter {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (!BooleanOption.BLOCK_COMMANDS.value()
            || !arenaRegistry.isInArena(player)
            || player.hasPermission(PlayingCommandPolicy.BYPASS_PERMISSION)
            || plugin.getPlayingCommandPolicy().isCommandAllowed(event.getMessage())) {
            return;
        }

        event.setCancelled(true);

        chatManager.sendMessage(player, "game.only-command-is-leave");
    }
}
