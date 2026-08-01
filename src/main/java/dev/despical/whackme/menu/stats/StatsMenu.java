package dev.despical.whackme.menu.stats;

import dev.despical.commons.configuration.ConfigUtils;
import dev.despical.fileitems.ItemManager;
import dev.despical.fileitems.SpecialItem;
import dev.despical.inventoryframework.Gui;
import dev.despical.inventoryframework.GuiItem;
import dev.despical.inventoryframework.pane.PaginatedPane;
import dev.despical.inventoryframework.pane.StaticPane;
import dev.despical.whackme.WhackMe;
import dev.despical.whackme.arena.Arena;
import dev.despical.whackme.chat.ChatManager;
import dev.despical.whackme.leaderboard.Leaderboard;
import dev.despical.whackme.leaderboard.LeaderboardEntry;
import dev.despical.whackme.menu.Menu;
import dev.despical.whackme.stats.StatisticType;
import dev.despical.whackme.stats.Statistics;
import dev.despical.whackme.stats.offline.OfflineStats;
import dev.despical.whackme.user.User;
import dev.despical.whackme.util.ItemUtils;
import dev.despical.whackme.util.Var;
import net.kyori.adventure.text.Component;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author Despical
 * <p>
 * Created at 2.06.2026
 */
public class StatsMenu implements Menu {

    private final WhackMe plugin;
    private final ItemManager itemManager;
    private final ChatManager chatManager;
    private final Gui gui;
    private final User viewer;
    private final OfflinePlayer target;
    private final String targetName;
    private final Var[] variables;
    private final Function<StatisticType<?>, Object> statProvider;

    public StatsMenu(User viewer) {
        this(viewer, viewer);
    }

    public StatsMenu(User viewer, User targetUser) {
        this.plugin = WhackMe.getInstance();
        this.itemManager = plugin.getItemManager();
        this.chatManager = plugin.getChatManager();
        this.viewer = viewer;
        this.target = targetUser.getPlayer();
        this.targetName = targetUser.getName();
        this.statProvider = targetUser::getStatistic;
        this.variables = createVars();
        this.gui = createBaseGui();
    }

    public StatsMenu(User viewer, OfflinePlayer offlinePlayer) {
        this.plugin = WhackMe.getInstance();
        this.itemManager = plugin.getItemManager();
        this.chatManager = plugin.getChatManager();

        this.viewer = viewer;
        this.target = offlinePlayer;

        OfflineStats stats = plugin.getStatsCacheManager().getStats(offlinePlayer);
        this.targetName = stats.getName();
        this.statProvider = stats::getStat;
        this.variables = createVars();
        this.gui = createBaseGui();
    }

    private Var[] createVars() {
        int plusBlocks = getIntStat(Statistics.PLUS_BLOCKS);
        int minusBlocks = getIntStat(Statistics.MINUS_BLOCKS);
        double hitRate = (plusBlocks + minusBlocks == 0) ? 100.0 : ((double) plusBlocks / (plusBlocks + minusBlocks)) * 100.0;

        return new Var[] {
            Var.ofPlayer(viewer),
            Var.of("%target%", targetName),
            Var.of("%games_played%", statProvider.apply(Statistics.GAMES_PLAYED)),
            Var.of("%perfect_runs%", statProvider.apply(Statistics.PERFECT_RUNS)),
            Var.of("%record_score%", statProvider.apply(Statistics.RECORD_SCORE)),
            Var.of("%longest_hit_streak%", statProvider.apply(Statistics.LONGEST_HIT_STREAK)),
            Var.of("%whacked_plus_blocks%", plusBlocks),
            Var.of("%whacked_minus_blocks%", minusBlocks),
            Var.of("%whacked_block_rate%", String.format("%.1f", hitRate))
        };
    }

    private Gui createBaseGui() {
        FileConfiguration config = ConfigUtils.getConfig(plugin, "menu/stats-menu");
        Component title = chatManager.parseMessage(config.getString("title", "").replace("%player%", targetName));

        Gui baseGui = new Gui(plugin, config.getInt("rows", 6), title);
        baseGui.setOnGlobalClick(event -> event.setCancelled(true));
        return baseGui;
    }

    @Override
    public void open() {
        Player player = viewer.getPlayer();
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.5f);

        openMainMenu();

        gui.show(player);
    }

    @Override
    public Gui getGui() {
        return gui;
    }

    private void openMainMenu() {
        gui.removePanes();

        Player player = viewer.getPlayer();
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);

        StaticPane pane = new StaticPane(0, 0, 9, gui.getRows());

        for (SpecialItem specialItem : itemManager.getItemsFromCategory("stats-menu-items")) {
            String id = specialItem.getKey();
            if (id.equals("arena_template") || id.equals("checkpoint_template") || id.equals("back_button") || id.equals("next_page") || id.equals("previous_page")) continue;

            ItemStack item = ItemUtils.formatItemStack(specialItem, variables);
            ItemUtils.applyPlayerProfileIfSkull(target, item);

            Consumer<InventoryClickEvent> action = event -> event.setCancelled(true);
            if ("open_arenas".equals(specialItem.getCustomKey("action"))) {
                action = _ -> openArenasMenu();
            } else if ("close_menu".equals(specialItem.getCustomKey("action"))) {
                action = _ -> player.closeInventory();
            }

            GuiItem guiItem = new GuiItem(item, action);
            placeItemInPane(pane, specialItem, guiItem);
        }

        gui.addPane(pane);
        gui.update();
    }

    private void openArenasMenu() {
        if (plugin.getArenaRegistry().getArenas().isEmpty()) {
            Player player = viewer.getPlayer();
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);

            chatManager.sendMessage(player, "stats-menu.no-arenas");
            return;
        }

        gui.removePanes();

        Player player = viewer.getPlayer();
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);

        StaticPane bgPane = createBackgroundPane("openMainMenu");
        PaginatedPane pages = new PaginatedPane(1, 2, 7, 3);
        List<GuiItem> arenaItems = new ArrayList<>();

        SpecialItem template = itemManager.getItemFromCategory("stats-menu-items", "arena_template");
        if (template != null) {
            String noRecord = getTemplateValue(template, "no-record");

            @SuppressWarnings("unchecked")
            Map<String, Integer> bestScores = (Map<String, Integer>) statProvider.apply(Statistics.ARENA_BEST_SCORES);

            for (Arena arena : plugin.getArenaRegistry().getArenas()) {
                int bestScore = bestScores.getOrDefault(arena.getId(), 0);
                Var[] vars = {
                    Var.of("%arena%", arena.getId()),
                    Var.of("%record_score%", bestScore > 0 ? bestScore : noRecord)
                };

                ItemStack item = ItemUtils.formatItemStack(template, vars);
                ItemMeta meta = item.getItemMeta();

                if (meta != null && meta.hasLore()) {
                    List<Component> lore = new ArrayList<>(meta.lore());
                    createArenaLore(arena.getId(), template, lore);

                    meta.lore(lore);
                    item.setItemMeta(meta);
                }

                arenaItems.add(new GuiItem(item, _ -> {}));
            }
        }

        pages.populateWithGuiItems(arenaItems);
        setupPaginationButtons(bgPane, pages);

        gui.addPane(bgPane);
        gui.addPane(pages);
        gui.update();
    }

    private void createArenaLore(String arenaId, SpecialItem template, List<Component> lore) {
        Leaderboard<Integer> board = plugin.getLeaderboardManager().getLeaderboard("arena_score_" + arenaId);

        if (board == null || board.sortedEntries().isEmpty()) {
            lore.add(chatManager.parseMessage(getTemplateValue(template, "empty-leaderboard")));
            return;
        }

        int targetRank = -1;

        for (int i = 0; i < Math.min(3, board.sortedEntries().size()); i++) {
            LeaderboardEntry<Integer> entry = board.sortedEntries().get(i);

            String color = getTemplateValue(template, "color-" + (i + 1));
            if (color == null) color = "<gray>";

            lore.add(chatManager.parseMessage(
                getTemplateValue(template, "leaderboard-entry")
                    .replace("%color%", color),
                Var.of("%rank%", i + 1),
                Var.of("%player%", entry.name()),
                Var.of("%record_score%", entry.value())
            ));

            if (entry.uuid().equals(target.getUniqueId())) {
                targetRank = i + 1;
            }
        }

        if (targetRank == -1) {
            for (int i = 3; i < board.sortedEntries().size(); i++) {
                if (board.sortedEntries().get(i).uuid().equals(target.getUniqueId())) {
                    lore.add(chatManager.parseMessage(getTemplateValue(template, "leaderboard-separator")));

                    lore.add(chatManager.parseMessage(
                        getTemplateValue(template, "leaderboard-player-entry"),
                        Var.of("%rank%", i + 1),
                        Var.of("%player%", targetName),
                        Var.of("%record_score%", board.sortedEntries().get(i).value())
                    ));
                    break;
                }
            }
        }
    }

    private StaticPane createBackgroundPane(String backAction) {
        StaticPane pane = new StaticPane(0, 0, 9, gui.getRows());

        for (SpecialItem specialItem : itemManager.getItemsFromCategory("stats-menu-items")) {
            switch (specialItem.getKey()) {
                case "background_fill", "decoration_borders" ->
                    placeItemInPane(pane, specialItem, new GuiItem(ItemUtils.formatItemStack(specialItem), event -> event.setCancelled(true)));
                case "player_head" -> {
                    ItemStack item = ItemUtils.formatItemStack(specialItem, variables);
                    ItemUtils.applyPlayerProfileIfSkull(target, item);

                    placeItemInPane(pane, specialItem, new GuiItem(item, event -> event.setCancelled(true)));
                }
                case "back_button" -> {
                    Consumer<InventoryClickEvent> action = backAction.equals("openMainMenu") ? _ -> openMainMenu() : _ -> openArenasMenu();
                    placeItemInPane(pane, specialItem, new GuiItem(ItemUtils.formatItemStack(specialItem), action));
                }
            }
        }

        return pane;
    }

    private void setupPaginationButtons(StaticPane pane, PaginatedPane pages) {
        SpecialItem next = itemManager.getItemFromCategory("stats-menu-items", "next_page");
        SpecialItem prev = itemManager.getItemFromCategory("stats-menu-items", "previous_page");
        SpecialItem bg = itemManager.getItemFromCategory("stats-menu-items", "background_fill");
        GuiItem bgItem = (bg != null) ? new GuiItem(ItemUtils.formatItemStack(bg), e -> e.setCancelled(true)) : null;

        if (pages.getPages() <= 1) return;

        if (next != null) {
            if (pages.getPage() < pages.getPages() - 1) {
                placeItemInPane(pane, next, new GuiItem(ItemUtils.formatItemStack(next), _ -> {
                    pages.setPage(pages.getPage() + 1);
                    setupPaginationButtons(pane, pages);

                    gui.update();

                    viewer.getPlayer().playSound(viewer.getPlayer().getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                }));
            } else {
                placeItemInPane(pane, next, bgItem);
            }
        }

        if (prev != null) {
            if (pages.getPage() > 0) {
                placeItemInPane(pane, prev, new GuiItem(ItemUtils.formatItemStack(prev), _ -> {
                    pages.setPage(pages.getPage() - 1);
                    setupPaginationButtons(pane, pages);

                    gui.update();

                    viewer.getPlayer().playSound(viewer.getPlayer().getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                }));
            } else {
                placeItemInPane(pane, prev, bgItem);
            }
        }
    }

    private String getTemplateValue(SpecialItem item, String key) {
        return item.getCustomKey(key);
    }

    private int getIntStat(StatisticType<?> statistic) {
        Object value = statProvider.apply(statistic);
        return value instanceof Number number ? number.intValue() : 0;
    }

    private void placeItemInPane(StaticPane pane, SpecialItem specialItem, GuiItem guiItem) {
        List<Integer> slots = specialItem.getCustomKey("slots");

        if (slots != null) {
            slots.forEach(slot -> pane.addItem(guiItem, slot % 9, slot / 9));
        } else {
            Integer slot = specialItem.getCustomKey("slot");
            if (slot != null) {
                pane.addItem(guiItem, slot % 9, slot / 9);
            }
        }
    }
}
