package dev.despical.whackme.setup;

import dev.despical.commons.configuration.ConfigUtils;
import dev.despical.inventoryframework.Gui;
import dev.despical.inventoryframework.pane.PaginatedPane;
import dev.despical.whackme.WhackMe;
import dev.despical.whackme.arena.Arena;
import dev.despical.whackme.menu.Menu;
import dev.despical.whackme.setup.pages.LocationsPage;
import dev.despical.whackme.setup.pages.PointBlockAppearancePage;
import dev.despical.whackme.setup.pages.PointBlockSettingsPage;
import dev.despical.whackme.setup.pages.SongSelectionPage;
import dev.despical.whackme.setup.pages.SetupHomePage;
import dev.despical.whackme.user.User;
import dev.despical.whackme.util.Var;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * @author Despical
 * <p>
 * Created at 20.06.2022
 */
@Getter
public class SetupMenu implements Menu {

    private final Gui gui;
    private final User user;
    private final Arena arena;
    private final Map<Integer, Supplier<SetupPage>> pages;

    private PaginatedPane basePane;

    public SetupMenu(WhackMe plugin, Arena arena, Player player) {
        this.user = plugin.getUserManager().getUser(player);
        this.arena = arena;
        this.pages = new HashMap<>();

        FileConfiguration config = ConfigUtils.getConfig(plugin, "menu/setup-menu");
        Component title = plugin.getChatManager().parseMessage(config.getString("title"), Var.of("%arena_id%", arena.getId()));

        this.gui = new Gui(plugin, 5, title);

        pages.put(0, () -> new SetupHomePage(this));
        pages.put(1, () -> new LocationsPage(this));
        pages.put(2, () -> new PointBlockSettingsPage(this));
        pages.put(3, () -> new PointBlockAppearancePage(this));

        setPage(0);
        open();
    }

    public void setPage(int page) {
        gui.removePanes();

        basePane = new PaginatedPane(9, 6);
        gui.addPane(basePane);

        SetupPage setupPage = pages.get(page).get();
        setupPage.configure(gui);
        setupPage.beforeOpening(gui);
        setupPage.injectItems(basePane);

        basePane.setPage(0);
        gui.update();
    }

    public void openSongSelection() {
        gui.removePanes();

        basePane = new PaginatedPane(9, 6);
        gui.addPane(basePane);

        SetupPage songPage = new SongSelectionPage(this);
        songPage.configure(gui);
        songPage.beforeOpening(gui);
        songPage.injectItems(basePane);

        basePane.setPage(0);
        gui.update();
    }

    @Override
    public void open() {
        Player player = user.getPlayer();
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.5f);
        gui.show(player);
    }
}
