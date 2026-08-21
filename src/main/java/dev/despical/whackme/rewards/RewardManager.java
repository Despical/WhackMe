package dev.despical.whackme.rewards;

import dev.despical.commons.configuration.ConfigUtils;
import dev.despical.commons.util.Strings;
import dev.despical.whackme.WhackMe;
import dev.despical.whackme.arena.blocks.PointBlockType;
import dev.despical.whackme.game.Game;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Loads and executes commands for reward triggers.
 *
 * @author Despical
 * <p>
 * Created at 13.07.2026
 */
public final class RewardManager {

    private volatile RewardConfiguration configuration;
    private final WhackMe plugin;

    public RewardManager(WhackMe plugin) {
        this.plugin = plugin;
        this.reload();
    }

    public void reload() {
        FileConfiguration config = ConfigUtils.getConfig(plugin, "rewards");

        if (!config.getBoolean("enabled", true)) {
            configuration = new RewardConfiguration(false, Map.of());
            return;
        }

        EnumMap<RewardType, List<String>> commands = new EnumMap<>(RewardType.class);

        for (RewardType type : RewardType.values()) {
            commands.put(type, config.getStringList(type.getConfigurationPath()));
        }

        configuration = new RewardConfiguration(true, commands);
    }

    /**
     * Executes the commands configured for a reward type.
     *
     * @param type reward trigger
     * @param game active game containing the player and score data
     */
    public void dispatch(RewardType type, Game game) {
        dispatch(type, game, null);
    }

    /**
     * Executes the commands configured for a block reward type.
     *
     * @param type reward trigger
     * @param game active game containing the player and score data
     * @param blockType interacted block type, if applicable
     */
    public void dispatch(RewardType type, Game game, PointBlockType blockType) {
        RewardConfiguration snapshot = configuration;
        List<String> commands = snapshot.commands().getOrDefault(type, List.of());

        if (!snapshot.enabled() || commands.isEmpty()) {
            return;
        }

        Runnable execution = () -> {
            RewardContext context = RewardContext.from(type, game, blockType);

            if (context != null) {
                commands.forEach(command -> execute(command, context));
            }
        };

        if (Bukkit.isPrimaryThread()) {
            execution.run();
        } else {
            Bukkit.getScheduler().runTask(plugin, execution);
        }
    }

    private void execute(String configuredCommand, RewardContext context) {
        String command = configuredCommand.trim();
        if (command.isEmpty()) {
            return;
        }

        boolean playerCommand = command.regionMatches(true, 0, "p:", 0, 2);
        if (playerCommand) {
            command = command.substring(2).trim();
        }

        command = Strings.format(context.format(command));
        if (command.startsWith("/")) {
            command = command.substring(1);
        }

        if (command.isBlank()) {
            return;
        }

        if (playerCommand) {
            context.player().performCommand(command);
        } else {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        }
    }

    private record RewardConfiguration(boolean enabled, Map<RewardType, List<String>> commands) {
    }
}
