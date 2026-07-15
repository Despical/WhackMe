package dev.despical.whackme.setup.pages;

import dev.despical.commons.item.ItemBuilder;
import dev.despical.commons.serializer.InventorySerializer;
import dev.despical.fileitems.SpecialItem;
import dev.despical.inventoryframework.Gui;
import dev.despical.inventoryframework.GuiItem;
import dev.despical.inventoryframework.pane.PaginatedPane;
import dev.despical.inventoryframework.pane.StaticPane;
import dev.despical.whackme.arena.options.ArenaKeys;
import dev.despical.whackme.setup.SetupMenu;
import dev.despical.whackme.setup.SetupPage;
import dev.despical.whackme.sign.SignManager;
import dev.despical.whackme.user.User;
import dev.despical.whackme.util.ItemUtils;
import dev.despical.whackme.util.Utils;
import dev.despical.whackme.util.Var;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.function.Consumer;

/**
 * @author Despical
 * <p>
 * Created at 02.06.2026
 */
public class SetupHomePage extends SetupPage {

    private final StaticPane pane;

    public SetupHomePage(SetupMenu menu) {
        super(menu);
        this.pane = new StaticPane(9, 5);
    }

    @Override
    public void beforeOpening(Gui gui) {
        gui.setRows(5);
    }

    @Override
    public void injectItems(PaginatedPane paginatedPane) {
        pane.addItem(createArenaLocationsItem(), 1, 1);
        pane.addItem(createPortalLayoutItem(), 3, 1);
        pane.addItem(createArenaSignItem(), 5, 1);
        pane.addItem(createPointBlocksItem(), 1, 3);
        pane.addItem(createSongSelectorItem(), 3, 3);
        pane.addItem(createOtherSettingsItem(), 7, 1);

        GuiItem arenaRecordResetItem = createArenaRecordResetItem();
        if (arenaRecordResetItem != null) {
            pane.addItem(arenaRecordResetItem, 5, 3);
        }

        if (!arena.getOption(ArenaKeys.READY)) {
            pane.addItem(createRegisterItem(), 8, 4);
        }

        paginatedPane.addPane(0, pane);
    }

    private GuiItem createArenaLocationsItem() {
        ItemStack item = itemManager.getItem("game-locations").getItemStack();

        return GuiItem.of(item, _ -> {
            var player = menu.getUser().getPlayer();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);

            menu.setPage(1);
        });
    }

    private GuiItem createPortalLayoutItem() {
        User user = menu.getUser();
        SpecialItem specialItem = itemManager.getItem("portal-layout");
        ItemStack item = specialItem.getItemStack().withType(user.isInEditingMode() ? Material.ENDER_EYE : Material.ENDER_PEARL);

        return GuiItem.of(item, event -> {
            menu.close();

            Runnable leaveEditing = () -> {
                user.setInEditingMode(false);
                user.sendRawMessage(specialItem.getCustomKey("leave-editing-message"));
                user.getPlayer().playSound(user.getPlayer().getLocation(), Sound.BLOCK_NOTE_BLOCK_BIT, 1f, 0.8f);

                menu.close();

                Player player = (Player) event.getWhoClicked();
                player.getInventory().clear();

                Utils.restoreSavedPlayerState(player);
            };

            if (user.isInEditingMode()) {
                leaveEditing.run();
                return;
            }

            Player player = (Player) event.getWhoClicked();
            InventorySerializer.saveInventoryToFile(plugin, player);

            Inventory inventory = player.getInventory();
            inventory.clear();
            inventory.setItem(4, new ItemBuilder(Material.BARRIER).name("<#FF5252>&lLeave Editing Mode").build());

            ItemStack endPortalFrame = ItemStack.of(Material.END_PORTAL_FRAME);
            for (int slot : List.of(0, 1, 2, 3, 5, 6, 7, 8)) {
                inventory.setItem(slot, endPortalFrame);
            }

            arena.setOption(ArenaKeys.CUSTOM, true);

            user.setInEditingMode(true);
            user.sendRawMessage(specialItem.getCustomKey("enter-editing-message"));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1f, 1.15f);

            Set<Location> locations = new HashSet<>(arena.getOption(ArenaKeys.PORTAL_LOCATIONS));

            plugin.getServer().getPluginManager().registerEvents(new Listener() {

                @EventHandler
                public void onLeaveEditing(PlayerInteractEvent event) {
                    User currentUser = plugin.getUserManager().getUser(event.getPlayer());

                    if (!currentUser.isInEditingMode()) return;
                    if (event.getAction() == Action.PHYSICAL) return;
                    if (event.getItem() == null || event.getItem().getType() != Material.BARRIER) return;

                    event.setCancelled(true);

                    leaveEditing.run();

                    HandlerList.unregisterAll(this);

                    arena.setOption(ArenaKeys.PORTAL_LOCATIONS, new ArrayList<>(locations));
                }

                @EventHandler
                public void onPortalPlacing(BlockPlaceEvent event) {
                    User currentUser = plugin.getUserManager().getUser(event.getPlayer());
                    if (!currentUser.isInEditingMode()) return;

                    locations.add(event.getBlock().getLocation());
                }

                @EventHandler
                public void onPortalBreaking(BlockBreakEvent event) {
                    User currentUser = plugin.getUserManager().getUser(event.getPlayer());

                    if (!currentUser.isInEditingMode()) return;
                    if (event.getBlock().getType() != Material.END_PORTAL_FRAME) return;

                    locations.remove(event.getBlock().getLocation());
                }
            }, plugin);
        });
    }

    private GuiItem createPointBlocksItem() {
        boolean hasPortalLocations = !arena.getOption(ArenaKeys.PORTAL_LOCATIONS).isEmpty();
        SpecialItem specialItem = itemManager.getItem(hasPortalLocations ? "point-blocks" : "point-blocks-locked");
        ItemStack item = specialItem.getItemStack();

        Consumer<InventoryClickEvent> consumer = event -> {
            Player player = (Player) event.getWhoClicked();

            if (!hasPortalLocations) {
                chatManager.sendRawMessage(player, specialItem.getCustomKey("message"));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                return;
            }

            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            menu.openPointBlockSettings(player);
        };

        return GuiItem.of(item, consumer);
    }

    private GuiItem createOtherSettingsItem() {
        ItemStack item = itemManager.getItem("other-settings").getItemStack();

        return GuiItem.of(item, event -> {
            Player player = (Player) event.getWhoClicked();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            menu.setPage(4);
        });
    }

    private GuiItem createArenaRecordResetItem() {
        int recordScore = arena.getOption(ArenaKeys.RECORD_SCORE);
        String recordHolder = arena.getOption(ArenaKeys.RECORD_HOLDER);

        if (recordScore < 0 || recordHolder == null || recordHolder.equalsIgnoreCase("None")) {
            return null;
        }

        SpecialItem specialItem = itemManager.getItem("arena-record-reset");
        ItemStack item = ItemUtils.formatItemStack(
            specialItem,
            Var.of("%record_holder%", recordHolder),
            Var.of("%record_score%", recordScore)
        );
        ItemUtils.applyArenaRecordResetHead(item, recordHolder);

        return GuiItem.of(item, event -> {
            Player player = (Player) event.getWhoClicked();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.15f);
            menu.setPage(5);
        });
    }

    private GuiItem createSongSelectorItem() {
        SpecialItem specialItem = itemManager.getItem("song-selector");
        ItemStack item = specialItem.getItemStack().clone();
        ItemMeta meta = item.getItemMeta();

        if (meta != null && meta.hasLore()) {
            String selectedSong = arena.getOption(ArenaKeys.ARENA_SONG);
            Var selectedSongVar = Var.of("%selected_song%", selectedSong != null ? selectedSong : "None");

            List<Component> lore = meta.lore();
            if (lore != null) {
                meta.lore(lore.stream()
                    .map(line -> chatManager.replaceVarsInComponent(line, selectedSongVar))
                    .toList());
            }

            item.setItemMeta(meta);
        }

        return GuiItem.of(item, event -> {
            Player player = (Player) event.getWhoClicked();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            menu.openSongSelection();
        });
    }

    private GuiItem createArenaSignItem() {
        SpecialItem specialItem = itemManager.getItem("arena-sign");
        ItemStack item = specialItem.getItemStack();
        Consumer<InventoryClickEvent> consumer = event -> {
            menu.close();

            Player player = (Player) event.getWhoClicked();
            Block block = player.getTargetBlock(null, 10);

            SignManager signManager = plugin.getSignManager();

            if (!(block.getState() instanceof Sign)) {
                signManager.sendMessage(player, "look-at-a-sign");
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                return;
            }

            if (signManager.isArenaSign(block)) {
                signManager.sendMessage(player, "already-game-sign");
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                return;
            }

            signManager.addArenaSign(arena, block);

            signManager.sendMessage(player, "created", signManager.getSignVars(block));

            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 2.0f);
            player.playSound(player.getLocation(), Sound.BLOCK_WOOD_PLACE, 1f, 0.8f);
        };

        return GuiItem.of(item, consumer);
    }

    private GuiItem createRegisterItem() {
        SpecialItem specialItem = itemManager.getItem("register-arena");
        ItemStack item = specialItem.getItemStack();

        Consumer<InventoryClickEvent> eventConsumer = event -> {
            menu.close();

            Player player = (Player) event.getWhoClicked();
            String missingInfo = null;

            if (arena.getOption(ArenaKeys.START_LOCATION) == null) {
                missingInfo = "Start Location";
            } else if (arena.getOption(ArenaKeys.END_LOCATION) == null) {
                missingInfo = "End Location";
            } else if (arena.getOption(ArenaKeys.PORTAL_LOCATIONS).isEmpty()) {
                missingInfo = "Portal Locations (at least 1 required)";
            }

            if (missingInfo != null) {
                List<String> errorMessages = specialItem.getCustomKey("missing-option");

                Var errorVar = Var.of("%option%", missingInfo);
                chatManager.sendCenteredMessage(player, errorMessages, errorVar);

                player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.5f, 0.5f);
                return;
            }

            arena.setOption(ArenaKeys.READY, true);
            arena.start();

            Var[] vars = {
                Var.of("%arena_id%", arena.getId()),
                Var.of("%command%", "/wm join ".concat(arena.getId()))
            };

            List<String> messages = specialItem.getCustomKey("registered-successfully");
            messages = messages.stream().map(line -> Utils.format(line, vars)).toList();

            chatManager.sendCenteredMessage(player, messages);
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
        };

        return GuiItem.of(item, eventConsumer);
    }
}
