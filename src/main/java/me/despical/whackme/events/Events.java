package me.despical.whackme.events;

import me.despical.commons.compat.XMaterial;
import me.despical.commons.serializer.InventorySerializer;
import me.despical.commons.util.UpdateChecker;
import me.despical.whackme.ConfigPreferences;
import me.despical.whackme.WhackMe;
import me.despical.whackme.arena.Arena;
import me.despical.whackme.user.User;
import me.despical.whackme.utils.Utils;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

/**
 * @author Despical
 * <p>
 * Created at 21.06.2022
 */
public class Events extends EventListener {

	public Events(WhackMe plugin) {
		super(plugin);
	}

	@EventHandler
	public void onCommandExecute(PlayerCommandPreprocessEvent event) {
		final Player player = event.getPlayer();

		if (!plugin.getArenaRegistry().isInArena(player)) {
			return;
		}

		if (!plugin.getOption(ConfigPreferences.Option.BLOCK_COMMANDS)) {
			return;
		}

		final String message = event.getMessage();

		if (plugin.getConfig().getStringList("Whitelisted-Commands").contains(message)) {
			return;
		}

		if (player.hasPermission("wm.admin")) {
			return;
		}

		if (message.startsWith("/wm") || message.startsWith("/whackme") || message.contains("top") || message.contains("stats")) {
			return;
		}

		event.setCancelled(true);
		player.sendMessage(chatManager.prefixedMessage("in_game.only_command_is_leave"));
	}

	@EventHandler
	public void onFoodLevelChange(FoodLevelChangeEvent event) {
		if (event.getEntity() instanceof Player && plugin.getArenaRegistry().isInArena((Player) event.getEntity())) {
			event.setFoodLevel(20);
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onBreak(BlockBreakEvent event) {
		if (plugin.getArenaRegistry().isInArena(event.getPlayer())) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onPlace(BlockPlaceEvent event) {
		if (plugin.getArenaRegistry().isInArena(event.getPlayer())) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onDrop(PlayerDropItemEvent event) {
		if (plugin.getArenaRegistry().isInArena(event.getPlayer())) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onInteract(PlayerInteractEvent event) {
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

		Block block = event.getClickedBlock();

		if (block == null || block.getType() != Utils.END_PORTAL_FRAME.getType()) {
			return;
		}

		ItemStack item = event.getItem();

		if (item == null || item.getType() != XMaterial.ENDER_EYE.parseMaterial()) return;

		if (plugin.getArenaRegistry().getArenas().stream().map(Arena::getLocations).anyMatch(location -> location.contains(block.getLocation()))) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onDamage(EntityDamageByEntityEvent event) {
		if (!(event.getEntity() instanceof Player)) return;

		Player player = (Player) event.getEntity();

		if (plugin.getArenaRegistry().isInArena(player)) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		final Player player = event.getPlayer();
		final User user = plugin.getUserManager().getUser(player);

		plugin.getUserManager().loadStatistics(user);

		if (plugin.getOption(ConfigPreferences.Option.INVENTORY_MANAGER_ENABLED)) {
			InventorySerializer.loadInventory(plugin, player);
		}

		if (!plugin.getOption(ConfigPreferences.Option.UPDATE_NOTIFIER_ENABLED)) return;
		if (!player.hasPermission("wm.update")) return;

		UpdateChecker.init(plugin, 104912).requestUpdateCheck().whenComplete((result, exception) -> {
			if (result.requiresUpdate()) {
				player.sendMessage(chatManager.coloredRawMessage("&3[Whack Me] &bFound an update: v" + result.getNewestVersion()));
			}
		});
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		final Player player = event.getPlayer();
		final Arena arena = plugin.getArenaRegistry().getArena(player);

		if (arena != null) {
			arena.removePlayer();
		}

		plugin.getUserManager().removeUser(player);
	}
}