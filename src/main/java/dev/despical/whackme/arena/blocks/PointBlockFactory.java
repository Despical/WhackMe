package dev.despical.whackme.arena.blocks;

import dev.despical.whackme.WhackMe;
import dev.despical.whackme.arena.Arena;
import org.bukkit.Location;

/**
 * @author Despical
 * <p>
 * Created at 14.07.2026
 */
final class PointBlockFactory {

    private final WhackMe plugin;
    private final Arena arena;
    private final PointBlockMessages messages;
    private final PointBlockTypeSelector typeSelector;
    private final PointBlockScoreService scoreService;

    PointBlockFactory(WhackMe plugin, Arena arena) {
        this.plugin = plugin;
        this.arena = arena;
        this.messages = PointBlockMessages.from(plugin.getChatManager());
        this.typeSelector = new PointBlockTypeSelector();
        this.scoreService = new PointBlockScoreService(plugin);
    }

    PointBlock create(PointHandler pointHandler, Location portalLocation) {
        PointBlockType type = typeSelector.select(pointHandler.getPointBlocks());
        PointBlockDisplay display = new PointBlockDisplay(arena, portalLocation, type, messages);

        return new PointBlock(
            plugin,
            pointHandler,
            portalLocation,
            display,
            PointBlockSettings.from(arena),
            scoreService
        );
    }
}
