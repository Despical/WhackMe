package dev.despical.whackme.handler.setup.components;

import dev.despical.commons.configuration.ConfigUtils;
import dev.despical.commons.serializer.LocationSerializer;
import dev.despical.inventoryframework.pane.PaginatedPane;
import dev.despical.whackme.WhackMe;
import dev.despical.whackme.arena.Arena;
import dev.despical.whackme.handler.setup.SetupInventory;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

/**
 * @author Despical
 * <p>
 * Created at 20.06.2022
 */
public abstract class SetupComponent {

    protected final String path;
    protected final Arena arena;
    protected final Player player;
    protected final WhackMe plugin;
    protected final SetupInventory setup;

    public SetupComponent(SetupInventory setup) {
        this.setup = setup;
        this.plugin = WhackMe.getInstance();
        this.player = setup.getPlayer();
        this.arena = setup.getArena();
        this.path = String.format("instances.%s.", arena.getId());
    }

    public abstract void injectComponents(PaginatedPane paginatedPane);

    protected final String isOptionDoneBool(String path) {
        FileConfiguration config = ConfigUtils.getConfig(plugin, "arenas");
        return config.isSet(path) ? LocationSerializer.isDefaultLocation(config.getString(path)) ? "&c✘ &lNot Completed" : "&a✔ &lCompleted" : "&c✘ &lNot Completed";
    }
}
