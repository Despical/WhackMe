package dev.despical.whackme.game;

import dev.despical.whackme.api.event.player.PlayerLeaveGameEvent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author Despical
 * <p>
 * Created at 4.06.2026
 */
@Getter
@RequiredArgsConstructor
public enum StopReason {

    ARENA_DELETED("game-stopped-due-to-arena-deletion", PlayerLeaveGameEvent.LeaveReason.ARENA_DELETED),
    SERVER_RELOAD("server-reload-detected", PlayerLeaveGameEvent.LeaveReason.RELOAD),
    SERVER_SHUTDOWN("server-shutdown-detected", PlayerLeaveGameEvent.LeaveReason.SHUTDOWN),
    STOP_COMMAND("game-stopped-by-command", PlayerLeaveGameEvent.LeaveReason.STOP_COMMAND);

    private final String messagePath;
    private final PlayerLeaveGameEvent.LeaveReason leaveReason;
}
