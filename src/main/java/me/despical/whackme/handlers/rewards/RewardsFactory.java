package me.despical.whackme.handlers.rewards;

import me.despical.commons.configuration.ConfigUtils;
import me.despical.whackme.WhackMe;
import me.despical.whackme.api.Reloadable;
import me.despical.whackme.api.statistics.StatisticType;
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
public class RewardsFactory implements Reloadable {

	private final WhackMe plugin;
	private final Set<Reward> rewards;

	public RewardsFactory(final WhackMe plugin) {
		this.plugin = plugin;
		this.rewards = new HashSet<>();
		this.registerRewards();
	}

	public void performReward(Player player, Reward.RewardType type) {
		List<Reward> rewardList = rewards.stream().filter(rew -> rew.getType() == type).collect(Collectors.toList());

		if (rewardList.isEmpty()) return;

		User user = plugin.getUserManager().getUser(player);
		int points = user.getStat(StatisticType.LOCAL_SCORE);

		for (Reward mainRewards : rewardList) {
			for (Reward.SubReward reward : mainRewards.getRewards()){
				if (ThreadLocalRandom.current().nextInt(0, 100) > reward.getChance()) continue;
				if (!reward.testPoints(points)) continue;

				String command = formatCommandPlaceholders(reward, user);
				int executor = reward.getExecutor();

				if (executor == 1) {
					plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), command);
					return;
				} else if (executor == 2) {
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
		formatted = formatted.replace("%points%", StatisticType.LOCAL_SCORE.from(user));
		formatted = formatted.replace("%point_streak%", StatisticType.LOCAL_STREAK.from(user));
		formatted = formatted.replace("%longest_point_streak%", StatisticType.LOCAL_LONGEST_STREAK.from(user));
		return formatted;
	}

	private void registerRewards() {
		FileConfiguration config = ConfigUtils.getConfig(plugin, "rewards");

		if (!config.getBoolean("rewards-enabled")) return;

		for (final Reward.RewardType rewardType : Reward.RewardType.values()) {
			rewards.add(new Reward(plugin, rewardType, config.getStringList(rewardType.path)));
		}
	}

	@Override
	public void reload() {
		this.rewards.clear();
		this.registerRewards();
	}
}