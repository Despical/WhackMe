package dev.despical.whackme.option;

/**
 * @author Despical
 * <p>
 * Created at 01.06.2026
 */
public enum BooleanOption implements ConfigOption<Boolean> {

    BLOCK_COMMANDS("game-settings.block-commands", true),
    BLOCK_OUTSIDE_CHAT("chat-settings.separate-chat", true),
    DISABLE_CHAT_IN_GAME("chat-settings.disable-chat-in-game", false),
    ENABLE_CHAT_FORMATTING("chat-settings.enable-formatting", true),
    BLOCK_LEAVE_COMMAND("game-settings.block-leave-command", false),
    DATABASE_ENABLED("database.enabled", false),
    POINT_BLOCKS_RUN_ASYNC("point-blocks.run-async", false),
    SCOREBOARD_ENABLED("scoreboard-enabled", true),
    DEBUG("debug", false),
    EVENT_PROFILING_ENABLED("event-profiling.enabled", false),
    EVENT_PROFILING_VERBOSE("event-profiling.verbose", false),
    UPDATE_NOTIFIER("update-notifier.enabled", true);

    private final String path;
    private final boolean defaultValue;

    BooleanOption(String path, boolean defaultValue) {
        this.path = path;
        this.defaultValue = defaultValue;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public Class<Boolean> getType() {
        return Boolean.class;
    }

    @Override
    public Boolean getDefaultValue() {
        return defaultValue;
    }
}
