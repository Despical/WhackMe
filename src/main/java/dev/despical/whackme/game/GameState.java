package dev.despical.whackme.game;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author Despical
 * <p>
 * Created at 13.07.2026
 */
@Getter
@RequiredArgsConstructor
public enum GameState {

    WAITING("waiting"),
    IN_GAME("in-game"),
    ENDING("ending"),
    RESTARTING("restarting"),
    INACTIVE("inactive");

    private final String path;
}
