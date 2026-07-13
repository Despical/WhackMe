package dev.despical.whackme.sign;

import dev.despical.whackme.WhackMe;
import dev.despical.whackme.arena.Arena;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

/**
 * @author Despical
 * <p>
 * Created at 12.12.2025
 */
public class ArenaSignEvents implements Listener {

    private final WhackMe plugin;
    private final SignManager signManager;

    public ArenaSignEvents(WhackMe plugin, SignManager signManager) {
        this.plugin = plugin;
        this.signManager = signManager;
    }

    @EventHandler
    public void onSignDestroy(BlockBreakEvent event) {
        Block block = event.getBlock();
        ArenaSign arenaSign = signManager.getArenaSignByBlock(block);

        if (arenaSign == null) {
            return;
        }

        Player player = event.getPlayer();
        if (!player.hasPermission("whackme.sign.break")) {
            event.setCancelled(true);

            signManager.sendMessage(player, "no-perm-to-break");
            return;
        }

        signManager.removeArenaSign(arenaSign);
        signManager.sendMessage(player, "removed", signManager.getSignVars(block));
    }

    @EventHandler
    public void onJoinAttempt(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        ArenaSign arenaSign = signManager.getArenaSignByBlock(event.getClickedBlock());
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || arenaSign == null) {
            return;
        }

        event.setCancelled(true);

        Arena arena = arenaSign.arena();
        if (arena == null) {
            return;
        }

        plugin.getArenaManager().joinAttempt(plugin.getUserManager().getUser(event.getPlayer()), arena);
    }
}
