package dev.despical.whackme.util;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author Despical
 * <p>
 * Created at 4.06.2026
 */
@NoArgsConstructor
public final class ShutdownDetector {

    @Getter
    private static volatile boolean shutdown;

    public static void init() {
        Runtime.getRuntime().addShutdownHook(
            new Thread(() -> shutdown = true)
        );
    }
}
