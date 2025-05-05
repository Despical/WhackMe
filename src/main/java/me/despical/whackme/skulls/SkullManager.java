package me.despical.whackme.skulls;

import me.despical.commons.XMaterial;
import me.despical.commons.item.ItemBuilder;
import me.despical.commons.item.ItemUtils;
import me.despical.whackme.WhackMe;
import me.despical.whackme.arena.Arena;
import me.despical.whackme.arena.blocks.PointBlockType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Despical
 * <p>
 * Created at 11.10.2024
 */
public class SkullManager {

    private final WhackMe plugin;
    private final Map<String, PointBlockData> blockData;

    public SkullManager(WhackMe plugin) {
        this.plugin = plugin;
        this.blockData = new HashMap<>();
        this.registerDefaultBlocks();
        this.registerCustomBlocks();
    }

    public ItemStack getPointBlock(Arena arena, PointBlockType type) {
        PointBlockData data = blockData.get(arena.getId());

        if (data == null) {
            return blockData.get(null).getPointBlock(type);
        }

        return data.getPointBlock(type);
    }

    private void registerDefaultBlocks() {
        PointBlockData data = new PointBlockData();

        for (PointBlockType type : PointBlockType.values()) {
            String blockName = plugin.getConfig().getString("Point-Blocks.Default." + type.getPath(), "");

            ItemStack pointBlock = blockName.startsWith("skull:") ? ItemUtils.getSkull(blockName.substring(6)) :
                XMaterial.matchXMaterial(blockName).orElseThrow(NullPointerException::new).parseItem();

            pointBlock = new ItemBuilder(pointBlock).lore(type.getTag()).build();

            data.addPointBlock(type, pointBlock);
        }

        blockData.put(null, data);
    }

    private void registerCustomBlocks() {
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("Point-Blocks.Custom");

        if (section == null) {
            return;
        }

        for (String id : section.getKeys(false)) {
            if (id.equals("default")) continue;

            if (!plugin.getArenaRegistry().isArena(id)) {
                plugin.getLogger().warning("Custom arena based skull loading failed! There is no arena called " + id + "!");
                continue;
            }

            PointBlockData data = new PointBlockData();

            for (PointBlockType type : PointBlockType.values()) {
                String blockName = plugin.getConfig().getString(String.format("Point-Blocks.Custom.%s.%s", id, type.getPath()));
                ItemStack pointBlock = blockName.startsWith("skull:") ? ItemUtils.getSkull(blockName.substring(6)) :
                    XMaterial.matchXMaterial(blockName).orElseThrow(NullPointerException::new).parseItem();

                pointBlock = new ItemBuilder(pointBlock).lore(type.getTag()).build();

                data.addPointBlock(type, pointBlock);
            }

            blockData.put(id, data);
        }
    }
}
