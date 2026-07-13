package dev.despical.whackme.sound;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Despical
 * <p>
 * Created at 13.07.2026
 */
@Getter
@AllArgsConstructor
public enum GameSound {

    POINT("point"),
    MINUS_POINT("minus-point"),
    GAME_FINISHED("game-finished"),
    RECORD_BROKEN("record-broken");

    private final String path;
}
