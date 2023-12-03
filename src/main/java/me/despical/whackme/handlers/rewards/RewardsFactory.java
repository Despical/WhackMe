package me.despical.whackme.handlers.rewards;

import me.despical.commons.configuration.ConfigUtils;
import me.despical.whackme.WhackMe;
import me.despical.whackme.api.StatsStorage;
import me.despical.whackme.arena.Arena;
import me.despical.whackme.user.User;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * @author Despical
 * <p>
 * Created at 21.08.2022
 */
public class RewardsFactory {

	private final WhackMe plugin;
	private final Set<Reward> rewards;

	public RewardsFactory(final WhackMe plugin) {
		this.plugin = plugin;
		this.rewards = new HashSet<>();

		registerRewards();
	}

	public void performReward(final Player player, final Reward.RewardType type) {
		final List<Reward> rewardList = rewards.stream().filter(rew -> rew.getType() == type).collect(Collectors.toList());

		if (rewardList.isEmpty()) return;

		for (final Reward mainRewards : rewardList) {
			for (final Reward.SubReward reward : mainRewards.getRewards()){
				if (ThreadLocalRandom.current().nextInt(0, 100) > reward.getChance()) continue;

				final String command = formatCommandPlaceholders(reward, plugin.getUserManager().getUser(player));

				switch (reward.getExecutor()) {
					case 1:
						plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), command);
						break;
					case 2:
						player.performCommand(command);
				}
			}
		}
	}

	private String formatCommandPlaceholders(final Reward.SubReward reward, final User user) {
		Arena arena = user.getArena();
		String formatted = reward.getExecutableCode();

		formatted = formatted.replace("%arena%", arena.getId());
		formatted = formatted.replace("%player%", user.getPlayer().getName());
		formatted = formatted.replace("%points%", Integer.toString(user.getStat(StatsStorage.StatisticType.LOCAL_SCORE)));
		return formatted;
	}

	private void registerRewards() {
		final FileConfiguration config = ConfigUtils.getConfig(plugin, "rewards");

		if (!config.getBoolean("rewards-enabled")) return;

		for (final Reward.RewardType rewardType : Reward.RewardType.values()) {
			rewards.add(new Reward(plugin, rewardType, config.getStringList(rewardType.path)));
		}
	}
}