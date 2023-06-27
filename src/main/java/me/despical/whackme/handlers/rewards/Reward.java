package me.despical.whackme.handlers.rewards;

import me.despical.whackme.Main;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Despical
 * <p>
 * Created at 21.08.2022
 */
public class Reward {

	private final RewardType type;
	private final List<SubReward> rewards;

	public Reward(final Main plugin, final RewardType type, final List<String> rawCodes) {
		this.type = type;
		this.rewards = new ArrayList<>();

		for (final String rawCode : rawCodes) {
			this.rewards.add(new SubReward(plugin, rawCode));
		}
	}

	public List<SubReward> getRewards() {
		return rewards;
	}

	public RewardType getType() {
		return type;
	}

	final static class SubReward {

		private String executableCode;
		private final int chance, executor;

		public SubReward(final Main plugin, final String rawCode) {
			var processedCode = rawCode;

			if (rawCode.contains("p:")) {
				this.executor = 2;

				processedCode = processedCode.replace("p:", "");
			} else if (rawCode.contains("script:")) {
				this.executor = 3;

				processedCode = processedCode.replace("script:", "");
			} else {
				this.executor = 1;
			}

			if (processedCode.contains("chance(")) {
				int loc = processedCode.indexOf(")");

				if (loc == -1) {
					plugin.getLogger().warning(String.format("Second '')'' is not found in chance condition! Command: %s", rawCode));

					this.chance = 101;
					return;
				}

				String chanceStr = processedCode;
				chanceStr = chanceStr.substring(0, loc).replaceAll("[^0-9]+", "");

				processedCode = processedCode.replace(String.format("chance(%s):", chanceStr), "");

				this.chance = Integer.parseInt(chanceStr);
			} else {
				this.chance = 100;
			}

			this.executableCode = processedCode;
		}

		public String getExecutableCode() {
			return executableCode;
		}

		public int getExecutor() {
			return executor;
		}

		public int getChance() {
			return chance;
		}
	}

	public enum RewardType {

		SUCCESSFUL_POINT("successful-point"),
		WRONG_POINT("wrong-point"),
		END_GAME("end-game");

		final String path;

		RewardType(String path) {
			this.path = "rewards." + path;
		}
	}
}