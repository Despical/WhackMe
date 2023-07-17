package me.despical.whackme.handlers.rewards;

import me.despical.commons.configuration.ConfigUtils;
import me.despical.whackme.Main;
import me.despical.whackme.api.StatsStorage;
import me.despical.whackme.user.User;
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

	public RewardsFactory(final Main plugin) {
		this.plugin = plugin;
		this.rewards = new HashSet<>();

		registerRewards();
	}

	public void performReward(final Player player, final Reward.RewardType type) {
		final var rewardList = rewards.stream().filter(rew -> rew.getType() == type).toList();

		if (rewardList.isEmpty()) return;

		for (final var mainRewards : rewardList) {
			for (final var reward : mainRewards.getRewards()){
				if (ThreadLocalRandom.current().nextInt(0, 100) > reward.getChance()) continue;

				final var command = formatCommandPlaceholders(reward, plugin.getUserManager().getUser(player));

				switch (reward.getExecutor()) {
					case 1 -> plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), command);
					case 2 -> player.performCommand(command);
				}
			}
		}
	}

	private String formatCommandPlaceholders(final Reward.SubReward reward, final User user) {
		var arena = user.getArena();
		var formatted = reward.getExecutableCode();

		formatted = formatted.replace("%arena%", arena.getId());
		formatted = formatted.replace("%player%", user.getPlayer().getName());
		formatted = formatted.replace("%points%", Integer.toString(user.getStat(StatsStorage.StatisticType.LOCAL_SCORE)));
		return formatted;
	}

	private void registerRewards() {
		final var config = ConfigUtils.getConfig(plugin, "rewards");

		if (!config.getBoolean("rewards-enabled")) return;

		for (final var rewardType : Reward.RewardType.values()) {
			rewards.add(new Reward(plugin, rewardType, config.getStringList(rewardType.path)));
		}
	}
}