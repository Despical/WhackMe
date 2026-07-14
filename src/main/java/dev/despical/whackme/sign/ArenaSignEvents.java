package dev.despical.whackme.sign;

import dev.despical.whackme.arena.Arena;
import dev.despical.whackme.event.ListenerAdapter;
import dev.despical.whackme.user.User;
import lombok.RequiredArgsConstructor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

/**
 * @author Despical
 * <p>
 * Created at 12.12.2025
 */
@RequiredArgsConstructor
public class ArenaSignEvents extends ListenerAdapter {

    private final SignManager signManager;

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
        if (event.getHand() != EquipmentSlot.HAND || event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Block block = event.getClickedBlock();
        if (block == null) {
            return;
        }

        ArenaSign arenaSign = signManager.getArenaSignByBlock(block);
        if (arenaSign == null) {
            return;
        }

        event.setCancelled(true);

        Arena arena = arenaSign.arena();
        if (arena == null) {
            return;
        }

        User user = userManager.getUser(event.getPlayer());
        arenaManager.joinAttempt(user, arena);
    }
}
