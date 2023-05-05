package me.despical.whackme.handlers.rewards;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;

/**
 * @author Despical
 * <p>
 * Created at 21.08.2022
 */
public class Reward {

	private String executableCode;

	private final RewardType type;
	private final RewardExecutor executor;
	private final double chance;

	public Reward(RewardType type, String rawCode) {
		this.type = type;
		String processedCode = rawCode;

		if (rawCode.contains("p:")) {
			this.executor = RewardExecutor.PLAYER;
			processedCode = StringUtils.replace(processedCode, "p:", "");
		} else if (rawCode.contains("script:")) {
			this.executor = RewardExecutor.SCRIPT;
			processedCode = StringUtils.replace(processedCode, "script:", "");
		} else {
			this.executor = RewardExecutor.CONSOLE;
		}

		if (processedCode.contains("chance(")) {
			final int loc = processedCode.indexOf(")");

			if (loc == -1) {
				Bukkit.getConsoleSender().sendMessage(String.format("&cRewards configuration is broken! Make sure you don't forget using ')' character in chance condition! Command: " + rawCode));
				this.chance = 101;
				return;
			}

			String chanceStr = processedCode;
			chanceStr = chanceStr.substring(0, loc).replaceAll("[^0-9]+", "");

			processedCode = StringUtils.replace(processedCode, "chance(" + chanceStr + "):", "");
			this.chance = Double.parseDouble(chanceStr);
		} else {
			this.chance = 100;
		}

		this.executableCode = processedCode;
	}

	public RewardExecutor getExecutor() {
		return executor;
	}

	public String getExecutableCode() {
		return executableCode;
	}

	public double getChance() {
		return chance;
	}

	public RewardType getType() {
		return type;
	}

	public enum RewardType {

		SUCCESSFUL_POINT, WRONG_POINT, END_GAME;

		public String getPath() {
			return "rewards." + name().toLowerCase().replace('_', '-');
		}
	}

	public enum RewardExecutor {
		CONSOLE, PLAYER, SCRIPT
	}
}