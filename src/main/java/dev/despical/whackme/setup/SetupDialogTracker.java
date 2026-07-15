package dev.despical.whackme.setup;

import dev.despical.whackme.WhackMe;
import io.papermc.paper.dialog.Dialog;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Despical
 * <p>
 * Created at 15.07.2026
 */
public final class SetupDialogTracker {

    private final WhackMe plugin;
    private final Set<UUID> viewers = ConcurrentHashMap.newKeySet();

    public SetupDialogTracker(WhackMe plugin) {
        this.plugin = plugin;
    }

    public void show(Player player, Dialog dialog) {
        if (!plugin.isEnabled() || !player.isOnline()) {
            return;
        }

        viewers.add(player.getUniqueId());
        player.showDialog(dialog);
    }

    public void release(Player player) {
        viewers.remove(player.getUniqueId());
    }

    public void closeAll() {
        for (UUID viewerId : viewers) {
            Player player = plugin.getServer().getPlayer(viewerId);
            if (player != null) {
                player.closeDialog();
            }
        }

        viewers.clear();
    }
}
