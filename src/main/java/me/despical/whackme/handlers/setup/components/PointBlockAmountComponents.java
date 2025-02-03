package me.despical.whackme.handlers.setup.components;

import me.despical.commons.XMaterial;
import me.despical.commons.configuration.ConfigUtils;
import me.despical.commons.item.ItemBuilder;
import me.despical.inventoryframework.GuiItem;
import me.despical.inventoryframework.pane.PaginatedPane;
import me.despical.inventoryframework.pane.StaticPane;
import me.despical.whackme.handlers.setup.SetupInventory;
import me.despical.whackme.user.User;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

/**
 * @author Despical
 * <p>
 * Created at 21.07.2024
 */
public class PointBlockAmountComponents extends SetupComponent {

    public PointBlockAmountComponents(SetupInventory setup) {
        super(setup);
    }

    @Override
    public void injectComponents(PaginatedPane paginatedPane) {
        StaticPane pane = new StaticPane(9, 3);
        User user = plugin.getUserManager().getUser(player);
        FileConfiguration config = ConfigUtils.getConfig(plugin, "arenas");
        ItemBuilder backgroundItem = new ItemBuilder(XMaterial.BLACK_STAINED_GLASS_PANE).name("&aSet min/max player amounts!");
        ItemBuilder minPointsItem = new ItemBuilder(XMaterial.GLOWSTONE_DUST)
            .name("&e&l Set Minimum Point Blocks Amount")
            .lore("&8• &7LEFT  click to increase")
            .lore("&8• &7RIGHT click to decrease", "")
            .lore("&8• &7Minimum amount of point blocks that")
            .lore("&7will be spawned around the player.")
            .amount(arena.getMinimumPoints());

        ItemBuilder maxPointsItem = new ItemBuilder(XMaterial.GLOWSTONE_DUST)
            .name("&e&l Set Maximum Point Blocks Amount")
            .lore("&8• &7LEFT  click to increase")
            .lore("&8• &7RIGHT click to decrease", "")
            .lore("&8• &7Maximum amount of point blocks that")
            .lore("&7will be spawned around the player.")
            .amount(arena.getMaximumPoints());

        pane.fillWith(backgroundItem.build(), event -> event.setCancelled(true));

        pane.addItem(GuiItem.of(minPointsItem.build(), event -> {
            int amount = event.getCurrentItem().getAmount();
            ItemStack item = event.getCurrentItem();
            ClickType click = event.getClick();

            item.setAmount(click.isRightClick() ? --amount : click.isLeftClick() ? ++amount : amount);

            if (item.getAmount() < 1) {
                user.sendRawMessage("&c&l✘ Minimum point block amount cannot be less than 1!");

                item.setAmount(amount = 1);
            }

            int size = config.getStringList(path + "portalLocations").size();

            if (size != 0 && amount > size) {
                user.sendRawMessage("&c&l✘ Minimum point block amount cannot be higher than portals amount!");

                item.setAmount(amount = size);
            } else if (item.getAmount() > arena.getMaximumPoints()) {
                user.sendRawMessage("&c&l✘ Minimum point block amount cannot be higher than maximum point block amount! Setting both as the same value!");

                arena.setMaximumPoints(amount);

                config.set(path + "minPoints", amount);

                item.setAmount(amount);
            }

            arena.setMinimumPoints(amount);

            config.set(path + "minPoints", amount);
            ConfigUtils.saveConfig(plugin, config, "arenas");

            new SetupInventory(plugin, arena, player, "Set Min/Max Point Block Amounts");
        }), 3, 1);

        pane.addItem(GuiItem.of(maxPointsItem.build(), event -> {
            int amount = event.getCurrentItem().getAmount();
            ItemStack item = event.getCurrentItem();
            ClickType click = event.getClick();

            item.setAmount(click.isRightClick() ? --amount : click.isLeftClick() ? ++amount : amount);

            int size = config.getStringList(path + "portalLocations").size();

            if (size != 0 && amount > size) {
                user.sendRawMessage("&c&l✘ Maximum point block amount cannot be higher than portals amount!");

                item.setAmount(amount = size);
            } else if (item.getAmount() < arena.getMinimumPoints()) {
                user.sendRawMessage("&c&l✘ Maximum point block amount cannot be less than minimum point block amount! Setting both as the same value!");

                arena.setMinimumPoints(amount);

                config.set(path + "minPoints", amount);

                item.setAmount(amount);
            }

            arena.setMaximumPoints(amount);

            config.set(path + "maxPoints", amount);
            ConfigUtils.saveConfig(plugin, config, "arenas");

            new SetupInventory(plugin, arena, player, "Set Min/Max Point Block Amounts");
        }), 5, 1);


        paginatedPane.addPane(1, pane);
    }
}
