package me.despical.whackme.handlers.rewards;

import me.despical.commons.configuration.ConfigUtils;
import me.despical.commons.engine.ScriptEngine;
import me.despical.commons.util.LogUtils;
import me.despical.whackme.ConfigPreferences;
import me.despical.whackme.Main;
import me.despical.whackme.api.StatsStorage;
import me.despical.whackme.arena.Arena;
import me.despical.whackme.arena.ArenaRegistry;
import org.apache.commons.lang.StringUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Despical
 * <p>
 * Created at 21.08.2022
 */
public class RewardsFactory {

	private final Main plugin;
	private final Set<Reward> rewards;

	public RewardsFactory(Main plugin) {
		this.plugin = plugin;
		this.rewards = new HashSet<>();

		registerRewards();
	}

	public void performReward(Player player, Reward.RewardType type) {
		if (rewards.isEmpty()) return;

		Arena arena = ArenaRegistry.getArena(player);

		for (Reward reward : rewards) {
			if (reward.getType() == type) {
				if (ThreadLocalRandom.current().nextInt(0, 100) > reward.getChance()) {
					continue;
				}

				String command = reward.getExecutableCode();
				command = formatCommandPlaceholders(command, arena, player);

				switch (reward.getExecutor()) {
					case CONSOLE:
						plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), command);
						break;
					case PLAYER:
						player.performCommand(command);
						break;
					case SCRIPT:
						ScriptEngine engine = new ScriptEngine();

						engine.setValue("arena", arena);
						engine.setValue("player", player);
						engine.setValue("server", plugin.getServer());
						engine.execute(command);
						break;
					default:
						break;
				}
			}
		}
	}

	private String formatCommandPlaceholders(String command, Arena arena, Player player) {
		String formatted = command;

		formatted = StringUtils.replace(formatted, "%arena%", arena.getId());
		formatted = StringUtils.replace(formatted, "%player%", player.getName());
		formatted = StringUtils.replace(formatted, "%points%", Integer.toString(plugin.getUserManager().getUser(player).getStat(StatsStorage.StatisticType.LOCAL_SCORE)));
		return formatted;
	}

	private void registerRewards() {
		if (!plugin.getConfigPreferences().getOption(ConfigPreferences.Option.REWARDS_ENABLED)) {
			return;
		}

		LogUtils.log("[Rewards Factory] Starting rewards registration");

		long start = System.currentTimeMillis();
		FileConfiguration config = ConfigUtils.getConfig(plugin, "rewards");

		for (Reward.RewardType rewardType : Reward.RewardType.values()) {
			for (String reward : config.getStringList(rewardType.getPath())) {
				rewards.add(new Reward(rewardType, reward));
			}
		}

		LogUtils.log("[Rewards Factory] Registered all rewards took {0} ms", System.currentTimeMillis() - start);
	}
}