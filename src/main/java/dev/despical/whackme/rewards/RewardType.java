package dev.despical.whackme.rewards;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Events that can execute configured reward commands.
 *
 * @author Despical
 * <p>
 * Created at 13.07.2026
 */
@Getter
@RequiredArgsConstructor
public enum RewardType {

    GAME_JOIN("game-join"),
    GAME_QUIT("game-quit"),
    GAME_END("game-end"),
    GREEN_BLOCK_STEP("green-block-step"),
    RED_BLOCK_STEP("red-block-step"),
    GRAY_BLOCK_STEP("gray-block-step");

    private final String configurationPath;
}
