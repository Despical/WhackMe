package me.despical.whackme.arena.blocks;

import me.despical.whackme.Main;
import me.despical.whackme.api.StatsStorage;
import me.despical.whackme.arena.Arena;
import me.despical.whackme.handler.SoundManager;
import me.despical.whackme.user.User;
import me.despical.whackme.util.Utils;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * @author Despical
 * <p>
 * Created at 20.06.2022
 */
public class PointBlock extends BukkitRunnable {

	private double y;
	private boolean forward = true;

	final ArmorStand stand;
	private final Main plugin;
	private final Arena arena;
	private final Location availableLocation;

	public PointBlock(Main plugin, Arena arena) {
		this.plugin = plugin;
		this.arena = arena;
		this.availableLocation = arena.getAvailableLocation();

		stand = (ArmorStand) availableLocation.getWorld().spawnEntity(availableLocation.clone().add(.5, -1.2, .5), EntityType.ARMOR_STAND);
		stand.setHelmet(getRandomItem());
		stand.setCustomName(getCustomName());
		stand.setCustomNameVisible(true);
		stand.setGravity(false);
		stand.setVisible(false);

		Utils.trySilently(consumer -> stand.setShieldBlockingDelay(1));

		arena.getPointBlocks().add(this);
		arena.getLocations().remove(availableLocation);

		registerEvent();
	}

	public void clear() {
		this.cancel();
		this.stand.remove();
	}

	private ItemStack getRandomItem() {
		final int greenSize = (int) arena.getPointBlocks().stream().filter(pointBlock -> pointBlock.stand.getHelmet().getType() == Utils.GREEN_TERRACOTTA.getType()).count(),
			redSize = (int) arena.getPointBlocks().stream().filter(pointBlock -> pointBlock.stand.getHelmet().getType() == Utils.RED_TERRACOTTA.getType()).count();

		if (greenSize > redSize) {
			return Utils.RED_TERRACOTTA;
		} else if (greenSize == redSize) {
			return Utils.GREEN_TERRACOTTA;
		}

		return Utils.GREEN_TERRACOTTA;
	}

	private String getCustomName() {
		return plugin.getChatManager().coloredRawMessage(stand.getHelmet().getType() == Utils.GREEN_TERRACOTTA.getType() ? plugin.getChatManager().message("point_blocks.punch_me") :
			plugin.getChatManager().message("point_blocks.dont_punch_me"));
	}

	private void registerEvent() {
		plugin.getServer().getPluginManager().registerEvents(new Listener() {

			@EventHandler
			public void onArmorStandManipulate(EntityDamageByEntityEvent event) {
				if (!(event.getDamager() instanceof Player)) return;
				if (!(event.getEntity() instanceof ArmorStand)) return;

				Player player = (Player) event.getDamager();

				if (!arena.containPlayer(player)) return;

				ArmorStand armorStand = (ArmorStand) event.getEntity();

				if (!armorStand.equals(stand)) return;

				User user = plugin.getUserManager().getUser(player);

				if (stand.getHelmet().getType() == Utils.GREEN_TERRACOTTA.getType()) {
					user.addStat(StatsStorage.StatisticType.LOCAL_SCORE, 1);

					plugin.getSoundManager().playSound(player, SoundManager.GameSounds.POINT_SOUND);
				} else if (stand.getHelmet().getType() == Utils.RED_TERRACOTTA.getType()) {
					user.addStat(StatsStorage.StatisticType.LOCAL_SCORE, -1);

					plugin.getSoundManager().playSound(player, SoundManager.GameSounds.MINUS_POINT_SOUND);
				}

				stand.setHelmet(Utils.CYAN_TERRACOTTA);
				stand.setCustomName(plugin.getChatManager().message("point_blocks.ouch"));
			}
		}, plugin);
	}

	@Override
	public void run() {
		if (forward) {
			y += 0.04;

			if (y > .65) {
				forward = false;
				return;
			}

			stand.teleport(stand.getLocation().clone().add(0, 0.04, 0));
		} else {
			y -= 0.04;

			if (y < -0.6) {
				forward = true;

				cancel();
				stand.remove();
				arena.getPointBlocks().remove(this);
				arena.getLocations().add(availableLocation);
				return;
			}

			stand.teleport(stand.getLocation().clone().subtract(0, 0.04, 0));
		}
	}
}