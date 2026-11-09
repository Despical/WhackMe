package dev.despical.whackme.arena.blocks;

import dev.despical.whackme.arena.Arena;
import dev.despical.whackme.arena.options.ArenaKeys;
import dev.despical.whackme.WhackMe;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.persistence.PersistentDataType;

/**
 * @author Despical
 * <p>
 * Created at 14.07.2026
 */
final class PointBlockDisplay {

    private static final NamespacedKey MANAGED_KEY = new NamespacedKey(WhackMe.getInstance(), "point_block_display");

    private final Arena arena;
    private final PointBlockMessages messages;
    private final PointBlockType type;
    private final ArmorStand stand;
    // Tracked here so moving the stand does not read and copy its location every tick.
    private final Location position;

    PointBlockDisplay(Arena arena, Location portalLocation, PointBlockType type, PointBlockMessages messages) {
        this.arena = arena;
        this.messages = messages;
        this.type = type;
        this.position = portalLocation.clone().add(.5, -1.2, .5);
        this.stand = (ArmorStand) portalLocation.getWorld().spawnEntity(position.clone(), EntityType.ARMOR_STAND);

        configureStand();
    }

    PointBlockType getType() {
        return type;
    }

    boolean represents(Entity entity) {
        return isManaged(entity) && stand.equals(entity);
    }

    static boolean isManaged(Entity entity) {
        return entity != null
            && entity.getPersistentDataContainer().has(MANAGED_KEY, PersistentDataType.BYTE);
    }

    boolean isAlreadyHit() {
        return messages.ouch().equals(stand.customName());
    }

    void showHitState() {
        stand.getEquipment().setHelmet(arena.getOption(ArenaKeys.GRAY_BLOCK_ITEM).clone());
        stand.customName(messages.ouch());
    }

    void moveVertically(double amount) {
        position.add(0, amount, 0);

        if (Bukkit.isPrimaryThread()) {
            // A main-thread teleport copies the coordinates right away, so the tracked location can be reused.
            stand.teleport(position);
        } else {
            stand.teleportAsync(position.clone());
        }
    }

    void remove() {
        stand.remove();
    }

    private void configureStand() {
        stand.getEquipment().setHelmet(arena.getOption(type.getArenaOption()).clone());
        stand.customName(type == PointBlockType.GREEN_BLOCK ? messages.punchMe() : messages.dontPunchMe());
        stand.setCustomNameVisible(true);
        stand.setGravity(false);
        stand.setVisible(false);
        stand.setSilent(true);
        stand.setPersistent(false);
        stand.getPersistentDataContainer().set(MANAGED_KEY, PersistentDataType.BYTE, (byte) 1);
    }
}
