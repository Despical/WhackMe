package me.despical.whackme.handlers.rewards;

import me.despical.whackme.WhackMe;

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

    public Reward(WhackMe plugin, RewardType type, List<String> rawCodes) {
        this.type = type;
        this.rewards = new ArrayList<>();

        for (final String rawCode : rawCodes) {
            this.rewards.add(new SubReward(plugin, rawCode));
        }
    }

    List<SubReward> getRewards() {
        return rewards;
    }

    public RewardType getType() {
        return type;
    }

    public enum RewardType {

        SUCCESSFUL_POINT("successful-point"),
        WRONG_POINT("wrong-point"),
        END_GAME("end-game"),
        NEW_RECORD("new-record");

        final String path;

        RewardType(String path) {
            this.path = "rewards." + path;
        }
    }

    static final class SubReward {

        private final int chance, executor, minimumPoints;
        private String executableCode;

        public SubReward(final WhackMe plugin, final String rawCode) {
            String processedCode = rawCode;

            if (rawCode.contains("p:")) {
                this.executor = 2;

                processedCode = processedCode.replace("p:", "");
            } else {
                this.executor = 1;
            }

            chance:
            if (processedCode.contains("chance(")) {
                int loc = processedCode.indexOf(")", processedCode.indexOf("chance("));

                if (loc == -1) {
                    plugin.getLogger().warning(String.format("Second '')'' is not found in chance condition! Command: %s", rawCode));

                    this.chance = 101;
                    break chance;
                }

                String chanceStr = processedCode;
                chanceStr = chanceStr.substring(processedCode.indexOf("chance("), loc).replaceAll("[^0-9]+", "");

                processedCode = processedCode.replace(String.format("chance(%s):", chanceStr), "");

                this.chance = Integer.parseInt(chanceStr);
            } else {
                this.chance = 100;
            }

            if (processedCode.contains("points(")) {
                int loc = processedCode.indexOf(")", processedCode.indexOf("points("));

                if (loc == -1) {
                    plugin.getLogger().warning(String.format("Second '')'' is not found in points condition! Command: %s", rawCode));

                    this.minimumPoints = -1;
                    return;
                }

                String pointsStr = processedCode;
                pointsStr = pointsStr.substring(processedCode.indexOf("points("), loc).replaceAll("[^0-9]+", "");

                processedCode = processedCode.replace(String.format("points(%s):", pointsStr), "");

                this.minimumPoints = Integer.parseInt(pointsStr);
            } else {
                this.minimumPoints = -1;
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

        public boolean testPoints(int points) {
            return minimumPoints == -1 || points >= minimumPoints;
        }
    }
}
