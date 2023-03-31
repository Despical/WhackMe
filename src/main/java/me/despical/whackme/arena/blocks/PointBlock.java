package me.despical.whackme.arena.blocks;

import me.despical.whackme.Main;
import me.despical.whackme.api.StatsStorage;
import me.despical.whackme.arena.Arena;
import me.despical.whackme.handlers.ChatManager;
import me.despical.whackme.handlers.SoundManager;
import me.despical.whackme.handlers.rewards.Reward;
import me.despical.whackme.user.User;
import me.despical.whackme.utils.Utils;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * @author Despical
 * <p>
 * Created at 20.06.2022
 */
public class PointBlock extends BukkitRunnable {

	private final static Main plugin = JavaPlugin.getPlugin(Main.class);
	private final static ChatManager chatManager = plugin.getChatManager();
	private final static ItemStack RED_BLOCK = plugin.getConfigPreferences().getRedBlock(), GREEN_BLOCK = plugin.getConfigPreferences().getGreenBlock(), CYAN_BLOCK = plugin.getConfigPreferences().getCyanBlock();

	private double y;
	private boolean forward = true;
	private Listener listener;

	final ArmorStand stand;
	private final Arena arena;
	private final Location availableLocation;
	private final double multiplier;

	public PointBlock(Arena arena) {
		this.arena = arena;
		this.multiplier = plugin.getConfigPreferences().getPointBlockMultiplier();
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

		registerEvents();
	}

	public void clear() {
		this.cancel();
		this.stand.remove();
		this.arena.getLocations().add(availableLocation);

		HandlerList.unregisterAll(listener);
	}

	private ItemStack getRandomItem() {
		final int greenSize = (int) arena.getPointBlocks().stream().filter(pointBlock -> pointBlock.stand.getHelmet().getData().equals(GREEN_BLOCK.getData())).count(),
			redSize = (int) arena.getPointBlocks().stream().filter(pointBlock -> pointBlock.stand.getHelmet().getData().equals(RED_BLOCK.getData())).count();

		if (greenSize > redSize) {
			return RED_BLOCK;
		} else if (greenSize == redSize) {
			return GREEN_BLOCK;
		}

		return GREEN_BLOCK;
	}

	private String getCustomName() {
		return chatManager.coloredRawMessage(stand.getHelmet().getType() == GREEN_BLOCK.getType() ? chatManager.message("point_blocks.punch_me") :
			chatManager.message("point_blocks.dont_punch_me"));
	}

	private void registerEvents() {
		plugin.getServer().getPluginManager().registerEvents(listener = new Listener() {

			@EventHandler
			public void onArmorStandManipulate(PlayerArmorStandManipulateEvent event) {
				final Player player = event.getPlayer();

				if (!arena.containPlayer(player)) return;

				final ArmorStand armorStand = event.getRightClicked();

				if (!armorStand.equals(stand)) return;

				event.setCancelled(true);
			}

			@EventHandler
			public void onArmorStandDamage(EntityDamageByEntityEvent event) {
				if (!(event.getDamager() instanceof Player)) return;
				if (!(event.getEntity() instanceof ArmorStand)) return;

				final Player player = (Player) event.getDamager();

				if (!arena.containPlayer(player)) return;

				final ArmorStand armorStand = (ArmorStand) event.getEntity();

				if (!armorStand.equals(stand)) return;

				final User user = plugin.getUserManager().getUser(player);
				final String name = stand.getCustomName();

				if (name == null) return;
				if (stand.getCustomName().equals(chatManager.message("point_blocks.ouch"))) return;

				if (name.equalsIgnoreCase(chatManager.message("point_blocks.punch_me"))) {
					user.addStat(StatsStorage.StatisticType.LOCAL_SCORE, 1);

					plugin.getSoundManager().playSound(player, SoundManager.GameSounds.POINT_SOUND);
					plugin.getRewardsFactory().performReward(player, Reward.RewardType.SUCCESSFUL_POINT);
				} else if (name.equalsIgnoreCase(chatManager.message("point_blocks.dont_punch_me"))) {
					user.addStat(StatsStorage.StatisticType.LOCAL_SCORE, -1);

					plugin.getSoundManager().playSound(player, SoundManager.GameSounds.MINUS_POINT_SOUND);
					plugin.getRewardsFactory().performReward(player, Reward.RewardType.WRONG_POINT);
				}

				stand.setHelmet(CYAN_BLOCK);
				stand.setCustomName(chatManager.message("point_blocks.ouch"));
			}
		}, plugin);
	}

	@Override
	public void run() {
		if (forward) {
			y += multiplier;

			if (y > .75) {
				forward = false;
				return;
			}

			stand.teleport(stand.getLocation().clone().add(0, multiplier, 0));
		} else {
			y -= multiplier;

			if (y < -0.6) {
				forward = true;

				cancel();
				stand.remove();
				arena.getPointBlocks().remove(this);
				arena.getLocations().add(availableLocation);
				return;
			}

			stand.teleport(stand.getLocation().clone().subtract(0, multiplier, 0));
		}
	}
}