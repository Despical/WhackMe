package dev.despical.whackme.util;

import dev.despical.commons.serializer.InventorySerializer;
import dev.despical.whackme.WhackMe;
import org.bukkit.entity.Player;

import java.io.File;

/**
 * Owns the durable inventory snapshots used while a player is in a game or
 * editing an arena.
 *
 * @author Despical
 * <p>
 * Created at 31.07.2026
 */
public final class PlayerInventoryManager {

    private final WhackMe plugin;
    private final File snapshotDirectory;

    public PlayerInventoryManager(WhackMe plugin) {
        this.plugin = plugin;
        this.snapshotDirectory = new File(plugin.getDataFolder(), "inventories");
    }

    public boolean save(Player player) {
        return InventorySerializer.saveInventoryToFile(plugin, player)
            && hasSnapshot(player);
    }

    public boolean hasSnapshot(Player player) {
        File snapshot = snapshotFile(player);
        return snapshot.isFile() && snapshot.length() > 0;
    }

    public boolean restore(Player player) {
        if (!hasSnapshot(player)) {
            return false;
        }

        player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
        InventorySerializer.loadInventory(plugin, player);

        return snapshotFile(player).delete();
    }

    private File snapshotFile(Player player) {
        return new File(snapshotDirectory, player.getUniqueId() + ".inventory");
    }
}
