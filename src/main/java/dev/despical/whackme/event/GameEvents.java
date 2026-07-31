package dev.despical.whackme.event;

import dev.despical.whackme.arena.Arena;
import dev.despical.whackme.arena.options.ArenaKeys;
import dev.despical.whackme.option.BooleanOption;
import dev.despical.whackme.radio.impl.NBAPIRadio;
import dev.despical.whackme.util.ItemUtils;
import dev.despical.whackme.util.Schedulers;
import dev.despical.whackme.util.Utils;
import dev.despical.whackme.util.Var;
import net.kyori.adventure.text.Component;
import dev.despical.whackme.user.User;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.PlayerInventory;

import java.util.*;

/**
 * @author Despical
 * <p>
 * Created at 21.06.2022
 */
public class GameEvents extends ListenerAdapter {

    private final Map<UUID, Arena> quitPlayers = new HashMap<>();

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        userManager.createNewUser(player);

        Arena arena = quitPlayers.remove(player.getUniqueId());
        if (!plugin.getPlayerInventoryManager().hasSnapshot(player)) {
            return;
        }

        Schedulers.runInTheNextTick(() -> {
            Optional.ofNullable(arena)
                .map(target -> target.getOption(ArenaKeys.END_LOCATION))
                .ifPresent(player::teleport);

            PlayerInventory inventory = player.getInventory();
            inventory.clear();
            inventory.setArmorContents(ItemUtils.EMPTY_ARMORS);

            plugin.getPlayerInventoryManager().restore(player);
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        if (plugin.getRadio() instanceof NBAPIRadio nbapiRadio) {
            nbapiRadio.stopPreview(player);
        }

        User user = userManager.getUser(player);
        UUID uuid = user.getUUID();

        Arena arena = user.getArena();
        if (arena != null) {
            quitPlayers.put(uuid, arena);
            arenaManager.quitPlayer(user, arena);
        }

        userManager.removeUser(user);

        plugin.getStatsCacheManager().invalidate(uuid);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        Arena arena = arenaRegistry.getArena(player);
        Set<Player> recipients = event.getRecipients();

        boolean blockOutsideChat = BooleanOption.BLOCK_OUTSIDE_CHAT.value();

        if (arena == null) {
            if (blockOutsideChat) {
                arenaRegistry.getArenas()
                    .stream()
                    .filter(other -> other.getPlayer() != null)
                    .forEach(recipients::remove);
            }

            return;
        }

        boolean disableChatInGame = BooleanOption.DISABLE_CHAT_IN_GAME.value();
        if (disableChatInGame) {
            event.setCancelled(true);
            chatManager.sendMessage(player, "game.chat-disabled-in-game");
            return;
        }

        boolean separateChat = BooleanOption.BLOCK_OUTSIDE_CHAT.value();
        boolean enableFormatting = BooleanOption.ENABLE_CHAT_FORMATTING.value();
        if (!enableFormatting && !separateChat) {
            return;
        }

        event.setCancelled(true);

        Component formattedMessage = enableFormatting
            ? chatManager.getMessageComponent(
                "chat-format",
                Var.of("%sender%", player.getName()),
                Var.of("%message%", event.getMessage())
            )
            : Component.text("<%s> %s".formatted(player.getName(), event.getMessage()));

        if (separateChat) {
            player.sendMessage(formattedMessage);
        } else {
            plugin.getServer().broadcast(formattedMessage);
        }
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player player && arenaRegistry.isInArena(player)) {
            event.setFoodLevel(20);
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        if (arenaRegistry.isInArena(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        if (arenaRegistry.isInArena(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        if (arenaRegistry.isInArena(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onSwap(PlayerSwapHandItemsEvent event) {
        if (arenaRegistry.isInArena(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player player && arenaRegistry.isInArena(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPickup(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player player && arenaRegistry.isInArena(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onGeneralDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player && arenaRegistry.isInArena(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        if (arenaRegistry.isInArena(player)) {
            event.setCancelled(true);
        }
    }
}
