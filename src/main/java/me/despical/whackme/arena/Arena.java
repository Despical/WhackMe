package me.despical.whackme.arena;

import me.despical.commons.serializer.InventorySerializer;
import me.despical.whackme.ConfigPreferences;
import me.despical.whackme.WhackMe;
import me.despical.whackme.api.event.arena.WMJoinEvent;
import me.despical.whackme.api.event.arena.WMLeaveEvent;
import me.despical.whackme.api.statistics.StatisticType;
import me.despical.whackme.arena.blocks.PointBlock;
import me.despical.whackme.arena.blocks.PointHandler;
import me.despical.whackme.arena.managers.BossBarManager;
import me.despical.whackme.arena.options.ArenaOption;
import me.despical.whackme.handlers.ChatManager;
import me.despical.whackme.handlers.rewards.Reward;
import me.despical.whackme.user.User;
import me.despical.whackme.utils.Utils;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * @author Despical
 * <p>
 * Created at 18.06.2022
 */
public class Arena extends BukkitRunnable {

	private static final WhackMe plugin = WhackMe.getInstance();

	private Player player;
	private boolean ready, custom, started;
	private List<Location> locations;

	private final String id;
	private final PointHandler pointHandler;
	private final BossBarManager bossBarManager;
	private final List<PointBlock> pointBlocks;
	private final Map<ArenaOption, Object> arenaOptions;
	private final Map<GameLocation, Location> gameLocations;

	public Arena(String id) {
		this.id = id;
		this.pointHandler = new PointHandler(this);
		this.bossBarManager = new BossBarManager(this);
		this.pointBlocks = new ArrayList<>();
		this.locations = new ArrayList<>();
		this.arenaOptions = new EnumMap<>(ArenaOption.class);
		this.gameLocations = new EnumMap<>(GameLocation.class);

		for (final ArenaOption option : ArenaOption.values()) {
			arenaOptions.put(option, option.getDefault());
		}
	}

	public String getId() {
		return id;
	}

	public boolean isReady() {
		return ready;
	}

	public void setReady(boolean ready) {
		this.ready = ready;
	}

	public boolean isCustom() {
		return custom;
	}

	public void setCustom(boolean custom) {
		this.custom = custom;
	}

	public Player getPlayer() {
		return player;
	}

	public String getPlayerName() {
		return player == null ? plugin.getChatManager().message("Placeholders.Unknown-Player") : player.getName();
	}

	public void addPlayer(Player player) {
		if (player == null) return;

		WMJoinEvent event = new WMJoinEvent(player, this);

		plugin.callEvent(event);

		if (event.isCancelled()) return;

		if (plugin.getOption(ConfigPreferences.Option.INVENTORY_MANAGER_ENABLED)) {
			InventorySerializer.saveInventoryToFile(plugin, player);
		}

		if (plugin.getOption(ConfigPreferences.Option.CLEAR_INVENTORY)) {
			player.getInventory().clear();
		}

		if (plugin.getOption(ConfigPreferences.Option.CLEAR_EFFECTS)) {
			player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
		}

		this.player = player;
		this.setTimer(ArenaOption.TIMER.getDefault());

		bossBarManager.addPlayer();

		User user = plugin.getUserManager().getUser(player);
		user.resetStats();
		user.updateAttackCooldown();

		plugin.getSignManager().updateSign(this);

		player.setFoodLevel(20);
		player.setGameMode(GameMode.ADVENTURE);
		player.teleport(getStartLocation());
		player.sendMessage(plugin.getChatManager().message("in_game.start_message"));
	}

	public void removePlayer() {
		this.removePlayer(true);
	}

	public void removePlayer(boolean teleportToEnd) {
		plugin.callEvent(new WMLeaveEvent(player, this));

		User user = plugin.getUserManager().getUser(player);
		ChatManager chatManager = plugin.getChatManager();
		int score = user.getStat(StatisticType.LOCAL_SCORE);

		if (score > user.getStat(StatisticType.RECORD_SCORE)) {
			user.setStat(StatisticType.RECORD_SCORE, score);

			plugin.getRewardsFactory().performReward(player, Reward.RewardType.NEW_RECORD);

			if (teleportToEnd) player.sendMessage(chatManager.message("in_game.finish_record_message").replace("%points%", Integer.toString(user.getStat(StatisticType.LOCAL_SCORE))));
		} else {
			if (teleportToEnd) player.sendMessage(chatManager.message("in_game.finish_message").replace("%points%", Integer.toString(user.getStat(StatisticType.LOCAL_SCORE))));
		}

		int localStreak = user.getStat(StatisticType.LOCAL_LONGEST_STREAK);

		if (localStreak > user.getStat(StatisticType.LONGEST_STREAK)) {
			user.setStat(StatisticType.LONGEST_STREAK, localStreak);
		}

		user.addStat(StatisticType.TOURS_PLAYED, 1);
		user.resetAttackCooldown();
		user.resetStats();
		user.setCooldown("play_again", plugin.getConfig().getInt("Game-Cooldown"));

		plugin.getUserManager().getUserDatabase().saveStatistics(user);

		if (plugin.getOption(ConfigPreferences.Option.CLEAR_INVENTORY)) {
			player.getInventory().clear();
		}

		if (plugin.getOption(ConfigPreferences.Option.INVENTORY_MANAGER_ENABLED)) {
			InventorySerializer.loadInventory(plugin, player);
		} else {
			player.setGameMode(GameMode.SURVIVAL);
		}

		bossBarManager.removePlayer();

		if (teleportToEnd) teleportToEndLocation();
		cleanGameArea();

		player = null;

		plugin.getSignManager().updateSign(this);
	}

	public void cleanGameArea() {
		this.pointBlocks.forEach(PointBlock::clear);
		this.pointBlocks.clear();
	}

	public BossBarManager getBossBarManager() {
		return bossBarManager;
	}

	public boolean containPlayer(Player player) {
		return this.player != null && this.player.getUniqueId().equals(player.getUniqueId());
	}

	public Location getStartLocation() {
		return gameLocations.get(GameLocation.START);
	}

	public void setStartLocation(Location location) {
		gameLocations.put(GameLocation.START, location);

		if (Utils.isSurroundedBy(location))
			locations = Utils.getBlocksSurroundedBy(location).stream().map(Block::getLocation).collect(Collectors.toList());
	}

	public Location getEndLocation() {
		return gameLocations.get(GameLocation.END);
	}

	public void setEndLocation(Location location) {
		gameLocations.put(GameLocation.END, location);
	}

	public void setLocations(List<Location> locations) {
		this.locations = locations;
	}

	public int getTimer() {
		return getOption(ArenaOption.TIMER);
	}

	public void setTimer(int timer) {
		setOptionValue(ArenaOption.TIMER, timer);
	}

	public int getMinimumPoints() {
		return getOption(ArenaOption.MINIMUM_POINTS);
	}

	public void setMinimumPoints(int points) {
		this.setOptionValue(ArenaOption.MINIMUM_POINTS, points);
	}

	public int getMaximumPoints() {
		return getOption(ArenaOption.MAXIMUM_POINTS);
	}

	public void setMaximumPoints(int points) {
		this.setOptionValue(ArenaOption.MAXIMUM_POINTS, points);
	}

	private int getOption(ArenaOption option) {
		return (int) arenaOptions.get(option);
	}

	private void setOptionValue(ArenaOption option, int value) {
		arenaOptions.put(option, value);
	}

	public void teleportToEndLocation() {
		if (player != null) {
			player.teleport(getEndLocation());
		}
	}

	public List<PointBlock> getPointBlocks() {
		return pointBlocks;
	}

	public List<Location> getLocations() {
		return locations;
	}

	public void start() {
		if (started) return;

		started = true;

		pointHandler.handleTask();

		runTaskTimer(plugin, 20L, 20L);
	}

	public Location getAvailableLocation() {
        return locations.isEmpty() ? null : locations.get(ThreadLocalRandom.current().nextInt(locations.size()));
	}

	@Override
	public void run() {
		if (player == null) return;

		int timer = getTimer() - 1;

		setTimer(timer);

		if (timer == -1) {
			plugin.getRewardsFactory().performReward(player, Reward.RewardType.END_GAME);

			removePlayer();
		}
	}

	@Override
	public String toString() {
		return id;
	}

	public enum GameLocation {
		START, END
	}
}