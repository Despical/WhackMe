package dev.despical.whackme.setup.pages;

import dev.despical.fileitems.SpecialItem;
import dev.despical.inventoryframework.Gui;
import dev.despical.inventoryframework.GuiItem;
import dev.despical.inventoryframework.pane.PaginatedPane;
import dev.despical.inventoryframework.pane.StaticPane;
import dev.despical.whackme.arena.options.ArenaKeys;
import dev.despical.whackme.radio.impl.NBAPIRadio;
import dev.despical.whackme.setup.SetupMenu;
import dev.despical.whackme.setup.SetupPage;
import dev.despical.whackme.util.Var;
import net.kyori.adventure.text.Component;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Despical
 * <p>
 * Created at 04.06.2026
 */
public class SongSelectionPage extends SetupPage {

    private static final int[][] SONG_SLOTS = {
        {1, 1}, {3, 1}, {5, 1}, {7, 1},
        {1, 3}, {3, 3}, {5, 3}, {7, 3}
    };

    public SongSelectionPage(SetupMenu menu) {
        super(menu);
    }

    @Override
    public void beforeOpening(Gui gui) {
        gui.setRows(6);
        gui.setOnClose(event -> stopPreview((Player) event.getPlayer()));
    }

    @Override
    public void injectItems(PaginatedPane paginatedPane) {
        if (plugin.getRadio() instanceof NBAPIRadio radio) {
            List<String> availableSongs = radio.getAvailableSongs();
            List<GuiItem> songItems = new ArrayList<>();

            songItems.add(createNoSongItem());
            availableSongs.forEach(songName -> songItems.add(createSongItem(songName)));

            int itemsPerPage = SONG_SLOTS.length;
            int pageCount = Math.max(1, (int) Math.ceil((double) songItems.size() / itemsPerPage));

            for (int pageIndex = 0; pageIndex < pageCount; pageIndex++) {
                StaticPane staticPane = new StaticPane(9, 6);
                int startIndex = pageIndex * itemsPerPage;
                int endIndex = Math.min(startIndex + itemsPerPage, songItems.size());

                for (int itemIndex = startIndex; itemIndex < endIndex; itemIndex++) {
                    int[] slot = SONG_SLOTS[itemIndex - startIndex];
                    staticPane.addItem(songItems.get(itemIndex), slot[0], slot[1]);
                }

                if (pageIndex > 0) {
                    staticPane.addItem(createPaginationItem(paginatedPane, pageIndex - 1, false), 2, 5);
                }

                if (pageIndex < pageCount - 1) {
                    staticPane.addItem(createPaginationItem(paginatedPane, pageIndex + 1, true), 6, 5);
                }

                staticPane.addItem(createSongSelectionBackItem(), 4, 5);
                paginatedPane.addPane(pageIndex, staticPane);
            }
            return;
        }

        StaticPane staticPane = new StaticPane(9, 6);
        staticPane.addItem(createNoteBlockAPIInfoItem(), 4, 2);
        staticPane.addItem(createSongSelectionBackItem(), 4, 5);
        paginatedPane.addPane(0, staticPane);
    }

    private GuiItem createNoSongItem() {
        SpecialItem specialItem = itemManager.getItem("song-no-selection");
        
        ItemStack item = specialItem.getItemStack().clone();
        ItemMeta meta = item.getItemMeta();

        if (meta != null && arena.getOption(ArenaKeys.ARENA_SONG) == null) {
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
        }

        return GuiItem.of(item, event -> {
            Player player = (Player) event.getWhoClicked();
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1.5f);

            stopPreview(player);

            arena.setOption(ArenaKeys.ARENA_SONG, null);
            menu.setPage(0);
        });
    }

    private GuiItem createSongItem(String songName) {
        SpecialItem specialItem = itemManager.getItem("song-item");
        ItemStack item = specialItem.getItemStack().clone();
        ItemMeta meta = item.getItemMeta();

        String currentSong = arena.getOption(ArenaKeys.ARENA_SONG);
        boolean isSelected = songName.equals(currentSong);

        if (meta != null) {
            if (meta.hasDisplayName()) {
                Component displayName = meta.displayName();

                if (displayName != null) {
                    Var songNameVar = Var.of("%song_name%", songName);
                    displayName = chatManager.replaceVarsInComponent(displayName, songNameVar);
                    meta.displayName(displayName);
                }
            }

            if (meta.hasLore()) {
                List<Component> lore = new ArrayList<>(meta.lore());

                Var songNameVar = Var.of("%song_name%", songName);
                lore = lore.stream()
                    .map(line -> chatManager.replaceVarsInComponent(line, songNameVar))
                    .toList();
            
                
                meta.lore(lore);
            }

            if (isSelected) {
                meta.addEnchant(Enchantment.UNBREAKING, 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }

            item.setItemMeta(meta);
        }

        return GuiItem.of(item, event -> {
            Player player = (Player) event.getWhoClicked();
            if (event.getClick() == ClickType.RIGHT) {
                boolean started = ((NBAPIRadio) plugin.getRadio()).previewSong(player, songName);

                player.playSound(player.getLocation(), started ? Sound.UI_BUTTON_CLICK : Sound.ENTITY_VILLAGER_NO, 1f, 1.2f);
                return;
            }

            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1.5f);
            stopPreview(player);
            
            arena.setOption(ArenaKeys.ARENA_SONG, songName);
            menu.setPage(0);
        });
    }

    private GuiItem createNoteBlockAPIInfoItem() {
        SpecialItem specialItem = itemManager.getItem("song-nbs-not-installed");
        ItemStack item = specialItem.getItemStack().clone();

        return GuiItem.of(item, event -> {
            event.setCancelled(true);
            
            Player player = (Player) event.getWhoClicked();
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
        });
    }

    private GuiItem createSongSelectionBackItem() {
        ItemStack item = itemManager.getItem("go-back").getItemStack();

        return GuiItem.of(item, event -> {
            Player player = (Player) event.getWhoClicked();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 0.6f);

            stopPreview(player);
            menu.setPage(0);
        });
    }

    private GuiItem createPaginationItem(PaginatedPane paginatedPane, int targetPage, boolean nextPage) {
        ItemStack item = new ItemStack(org.bukkit.Material.ARROW);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.displayName(Component.text(nextPage ? "Next Page" : "Previous Page"));
            item.setItemMeta(meta);
        }

        return GuiItem.of(item, event -> {
            Player player = (Player) event.getWhoClicked();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);

            stopPreview(player);
            paginatedPane.setPage(targetPage);
            menu.getGui().update();
        });
    }

    private void stopPreview(Player player) {
        if (plugin.getRadio() instanceof NBAPIRadio radio) {
            radio.stopPreview(player);
        }
    }
}
