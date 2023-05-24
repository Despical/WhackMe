package me.despical.whackme.arena;

import me.despical.commons.miscellaneous.AttributeUtils;
import me.despical.commons.serializer.InventorySerializer;
import me.despical.whackme.ConfigPreferences;
import me.despical.whackme.Main;
import me.despical.whackme.api.StatsStorage;
import me.despical.whackme.arena.blocks.PointBlock;
import me.despical.whackme.arena.blocks.PointHandler;
import me.despical.whackme.arena.managers.BossBarManager;
import me.despical.whackme.arena.options.ArenaOption;
import me.despical.whackme.handlers.rewards.Reward;
import me.despical.whackme.user.User;
import me.despical.whackme.utils.Utils;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Despical
 * <p>
 * Created at 18.06.2022
 */
public class Arena extends BukkitRunnable {

	private final static Main plugin = JavaPlugin.getPlugin(Main.class);

	private Player player;
	private boolean ready;
	private List<Location> locations;

	private final String id;
	private final Random random;
	private final PointHandler pointHandler;
	private final BossBarManager bossBarManager;
	private final List<PointBlock> pointBlocks;
	private final Map<ArenaOption, Integer> arenaOptions;
	private final Map<GameLocation, Location> gameLocations;

	public Arena(String id) {
		this.id = id;
		this.random = new Random();
		this.pointHandler = new PointHandler(plugin, this);
		this.bossBarManager = new BossBarManager(plugin, this);
		this.pointBlocks = new ArrayList<>();
		this.locations = new ArrayList<>();
		this.arenaOptions = new EnumMap<>(ArenaOption.class);
		this.gameLocations = new EnumMap<>(GameLocation.class);

		for (ArenaOption option : ArenaOption.values()) {
			arenaOptions.put(option, option.getDefaultValue());
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

	public int getProgress() {
		final boolean isLobbySet = getStartLocation() != null, isEndSet = getEndLocation() != null;

		return isLobbySet ? isEndSet ? 100 : 50 : isEndSet ? 50 : 0;
	}

	public Player getPlayer() {
		return player;
	}

	public void addPlayer(Player player) {
		if (player == null) return;

		if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.INVENTORY_MANAGER_ENABLED)) {
			InventorySerializer.saveInventoryToFile(plugin, player);
		}

		AttributeUtils.setAttackCooldown(player, plugin.getConfig().getDouble("Hit-Cooldown-Delay", 4));

		if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.CLEAR_INVENTORY)) {
			player.getInventory().clear();
		}

		if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.CLEAR_EFFECTS)) {
			player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
		}

		this.player = player;
		this.setTimer(ArenaOption.TIMER.getDefaultValue());

		bossBarManager.addPlayer();

		plugin.getUserManager().getUser(player).setStat(StatsStorage.StatisticType.LOCAL_SCORE, 0);

		player.setFoodLevel(20);
		player.setGameMode(GameMode.ADVENTURE);
		player.teleport(getStartLocation());
		player.sendMessage(plugin.getChatManager().message("in_game.start_message"));
	}

	public void removePlayer() {
		final User user = plugin.getUserManager().getUser(player);
		final int score = user.getStat(StatsStorage.StatisticType.LOCAL_SCORE);

		if (score > user.getStat(StatsStorage.StatisticType.RECORD_SCORE)) {
			user.setStat(StatsStorage.StatisticType.RECORD_SCORE, score);

			player.sendMessage(plugin.getChatManager().message("in_game.finish_record_message").replace("%points%", Integer.toString(plugin.getUserManager().getUser(player).getStat(StatsStorage.StatisticType.LOCAL_SCORE))));
		} else {
			player.sendMessage(plugin.getChatManager().message("in_game.finish_message").replace("%points%", Integer.toString(plugin.getUserManager().getUser(player).getStat(StatsStorage.StatisticType.LOCAL_SCORE))));
		}

		user.addStat(StatsStorage.StatisticType.TOURS_PLAYED, 1);

		plugin.getUserManager().getDatabase().saveAllStatistic(user);

		if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.CLEAR_INVENTORY)) {
			player.getInventory().clear();
		}

		if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.INVENTORY_MANAGER_ENABLED)) {
			InventorySerializer.loadInventory(plugin, player);
		} else {
			player.setGameMode(GameMode.SURVIVAL);
		}

		AttributeUtils.resetAttackCooldown(player);

		bossBarManager.removePlayer();

		teleportToEndLocation();
		cleanGameArea();

		player = null;
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

		if (Utils.isSurroundedBy(location)) locations = Utils.getBlocksSurroundedBy(location).stream().map(Block::getLocation).collect(Collectors.toList());
	}

	public Location getEndLocation() {
		return gameLocations.get(GameLocation.END);
	}

	public void setEndLocation(Location location) {
		gameLocations.put(GameLocation.END, location);
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

	public void setMinimumPoints(int minimumPoints) {
		setOptionValue(ArenaOption.MINIMUM_POINTS, Math.max(1, minimumPoints));
	}

	public int getMaximumPoints() {
		return getOption(ArenaOption.MAXIMUM_POINTS);
	}

	public void setMaximumPoints(int maximumPoints) {
		setOptionValue(ArenaOption.MAXIMUM_POINTS, Math.max(maximumPoints, 8));
	}

	private int getOption(ArenaOption option) {
		return arenaOptions.get(option);
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
		pointHandler.handleTask();

		runTaskTimer(plugin, 20L, 20L);
	}

	public Location getAvailableLocation() {
		return locations.isEmpty() ? null : locations.get(random.nextInt(locations.size()));
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

	public enum GameLocation {
		START, END
	}
}