package dev.despical.whackme.api;

import dev.despical.whackme.WhackMe;
import dev.despical.whackme.option.BooleanOption;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;
import java.util.logging.Level;

/**
 * Collects optional execution timings for Whack Me custom event listeners.
 * <p>
 * Profiling is disabled by default unless enabled through the plugin
 * configuration. Recorded data is held in memory for the lifetime of the
 * plugin and can be rendered to an authorized command sender.
 *
 * @author Despical
 * <p>
 * Created at 29.01.2026
 */
@ApiStatus.Internal
public final class EventProfiler {

    private boolean enabled;
    private boolean verbose;

    private final WhackMe plugin;
    private final Map<Class<? extends Event>, ProfileData> profiles;

    /**
     * Creates a profiler for the plugin event dispatcher.
     *
     * @param plugin the owning Whack Me instance
     */
    public EventProfiler(@NotNull WhackMe plugin) {
        this.plugin = plugin;
        this.profiles = new ConcurrentHashMap<>();
        this.reload();
    }

    /**
     * Returns whether event timing collection is enabled.
     *
     * @return {@code true} when event timings are being recorded
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Returns whether every event invocation is logged.
     *
     * @return {@code true} when verbose profiling is enabled
     */
    public boolean isVerbose() {
        return verbose;
    }

    /**
     * Reloads profiler switches from the active configuration.
     */
    public void reload() {
        this.enabled = BooleanOption.EVENT_PROFILING_ENABLED.value();
        this.verbose = BooleanOption.EVENT_PROFILING_VERBOSE.value();
    }

    /**
     * Records one completed custom event invocation.
     *
     * @param event the event that was dispatched
     * @param durationNanos listener execution time in nanoseconds
     */
    public void record(@NotNull Event event, long durationNanos) {
        if (!enabled) {
            return;
        }

        profiles.computeIfAbsent(event.getClass(), k -> new ProfileData())
            .record(durationNanos);

        if (verbose) {
            logEvent(event, durationNanos);
        }
    }

    /**
     * Sends accumulated timing totals and averages to a command sender.
     *
     * @param sender the report recipient
     */
    public void sendReport(@NotNull CommandSender sender) {
        if (!enabled) {
            plugin.getChatManager().sendRawMessage(sender,
                "<#FF1744>✖ <#ff5c5c>Event timings system is currently disabled."
            );

            plugin.getChatManager().sendRawMessage(sender,
                "<gray>➥ Set <yellow>event-profiling.enabled</yellow> to <green>true</green> in config.yml"
            );
            return;
        }

        plugin.getChatManager().sendRawMessage(sender,
            "<gray><strikethrough>──────</strikethrough> <gradient:#ffcc00:#ff8c00>Whack Me Event Timings</gradient> <gray><strikethrough>──────</strikethrough>"
        );

        if (profiles.isEmpty()) {
            plugin.getChatManager().sendRawMessage(sender,
                "<gray>• <#9e9e9e>No timing data collected yet.</#9e9e9e>"
            );
            return;
        }

        profiles.entrySet().stream()
            .sorted((a, b) -> Long.compare(b.getValue().totalTime(), a.getValue().totalTime()))
            .forEach(entry -> {
                ProfileData data = entry.getValue();

                double totalMs = data.totalTime() / 1_000_000.0;
                double avgMs = data.averageTime() / 1_000_000.0;

                String totalColor = gradientColorForMs(totalMs);
                String avgColor = gradientColorForMs(avgMs);

                String message = String.format(
                    Locale.US,
                    "<gray>• <#ffd54f>%s <gray>→ <%s>%.3f ms</%s> <gray>(avg <%s>%.3f ms</%s>, <#a5d6a7>%dx</#a5d6a7>)",
                    entry.getKey().getSimpleName(),
                    totalColor,
                    totalMs,
                    totalColor,
                    avgColor,
                    avgMs,
                    avgColor,
                    data.calls()
                );

                plugin.getChatManager().sendRawMessage(sender, message);
            });
    }

    /**
     * Maps a duration to a green-to-red MiniMessage color.
     *
     * @param ms duration in milliseconds
     * @return hexadecimal RGB color without MiniMessage brackets
     */
    private String gradientColorForMs(double ms) {
        double maxMs = 5.0;
        double ratio = Math.min(ms / maxMs, 1.0);

        int r = (int) (255 * ratio);
        int g = (int) (255 * (1 - ratio));
        int b = 90;

        return String.format("#%02x%02x%02x", r, g, b);
    }

    /**
     * Writes one verbose profiler entry to the plugin logger.
     *
     * @param event measured event
     * @param nanos dispatch duration in nanoseconds
     */
    private void logEvent(Event event, long nanos) {
        double ms = nanos / 1_000_000.0;

        String detail = buildEventDetails(event);

        plugin.getLogger().log(
            Level.INFO,
            "[Profiler] {0} fired in {1} ms{2}.",
            new Object[] {
                event.getClass().getSimpleName(),
                String.format(Locale.US, "%.3f", ms),
                detail
            }
        );
    }

    /**
     * Creates the contextual suffix appended to verbose log entries.
     *
     * @param event event whose debug representation is included
     * @return formatted event detail suffix
     */
    private String buildEventDetails(Event event) {
        return ", (" + event.toString() + ")";
    }

    /**
     * Thread-safe aggregate for one concrete event class.
     *
     * @param callAdder number of recorded dispatches
     * @param timeAdder total recorded duration in nanoseconds
     * @author Despical
     * <p>
     * Created at 29.01.2026
     */
    private record ProfileData(LongAdder callAdder, LongAdder timeAdder) {

        /**
         * Creates an empty aggregate.
         */
        ProfileData() {
            this(new LongAdder(), new LongAdder());
        }

        /**
         * Adds a completed dispatch to this aggregate.
         *
         * @param nanos dispatch duration in nanoseconds
         */
        void record(long nanos) {
            callAdder.add(1);
            timeAdder.add(nanos);
        }

        /**
         * Returns the number of recorded dispatches.
         *
         * @return dispatch count
         */
        long calls() {
            return callAdder.sum();
        }

        /**
         * Returns the combined duration of all recorded dispatches.
         *
         * @return total duration in nanoseconds
         */
        long totalTime() {
            return timeAdder.sum();
        }

        /**
         * Returns the average dispatch duration.
         *
         * @return average duration in nanoseconds, or zero when empty
         */
        long averageTime() {
            long count = calls();
            return count == 0 ? 0 : totalTime() / count;
        }
    }
}
