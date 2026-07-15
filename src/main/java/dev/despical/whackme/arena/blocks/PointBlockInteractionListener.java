package dev.despical.whackme.arena.blocks;

import dev.despical.whackme.WhackMe;
import dev.despical.whackme.arena.Arena;
import dev.despical.whackme.sound.GameSound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;

/**
 * @author Despical
 * <p>
 * Created at 14.07.2026
 */
final class PointBlockInteractionListener implements Listener {

    private final WhackMe plugin;
    private final PointHandler pointHandler;
    private final Arena arena;
    private final PointBlockDisplay display;
    private final PointBlockScoreService scoreService;
    private boolean registered;

    PointBlockInteractionListener(
        WhackMe plugin,
        PointHandler pointHandler,
        PointBlockDisplay display,
        PointBlockScoreService scoreService
    ) {
        this.plugin = plugin;
        this.pointHandler = pointHandler;
        this.arena = pointHandler.game().getArena();
        this.display = display;
        this.scoreService = scoreService;
    }

    void register() {
        if (registered) {
            return;
        }

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        registered = true;
    }

    void unregister() {
        if (!registered) {
            return;
        }

        HandlerList.unregisterAll(this);
        registered = false;
    }

    @EventHandler
    public void onArmorStandManipulate(PlayerArmorStandManipulateEvent event) {
        if (arena.isPlaying(event.getPlayer()) && display.represents(event.getRightClicked())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onArmorStandDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) {
            return;
        }

        if (!(event.getEntity() instanceof ArmorStand) || !display.represents(event.getEntity()) || !arena.isPlaying(player)) {
            return;
        }

        event.setCancelled(true);

        if (display.isAlreadyHit()) {
            plugin.getSoundManager().play(player, GameSound.OUCH);
            return;
        }

        scoreService.apply(player, display.getType());
        pointHandler.game().getScoreboardManager().update();
        display.showHitState();
    }
}
