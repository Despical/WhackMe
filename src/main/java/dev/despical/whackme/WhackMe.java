package dev.despical.whackme;

import dev.despical.commandframework.CommandArguments;
import dev.despical.commandframework.CommandFramework;
import dev.despical.commons.serializer.InventorySerializer;
import dev.despical.commons.util.UpdateChecker;
import dev.despical.whackme.api.event.WMEvent;
import dev.despical.whackme.api.statistics.StatisticType;
import dev.despical.whackme.arena.Arena;
import dev.despical.whackme.arena.ArenaRegistry;
import dev.despical.whackme.arena.managers.ArenaManager;
import dev.despical.whackme.command.AdminCommands;
import dev.despical.whackme.command.PlayerCommands;
import dev.despical.whackme.event.GameEvents;
import dev.despical.whackme.handler.ChatManager;
import dev.despical.whackme.handler.PlaceholderManager;
import dev.despical.whackme.handler.ReloadManager;
import dev.despical.whackme.handler.SoundManager;
import dev.despical.whackme.handler.rewards.Reward;
import dev.despical.whackme.handler.rewards.RewardsFactory;
import dev.despical.whackme.handler.sign.SignManager;
import dev.despical.whackme.leaderboard.LeaderboardManager;
import dev.despical.whackme.skull.SkullManager;
import dev.despical.whackme.user.User;
import dev.despical.whackme.user.UserManager;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * @author Despical
 * <p>
 * Created at 18.06.2022
 */
public class WhackMe extends JavaPlugin {

    private static WhackMe instance;

    private ChatManager chatManager;
    private CommandFramework commandFramework;
    private ConfigPreferences configPreferences;
    private SoundManager soundManager;
    private UserManager userManager;
    private RewardsFactory rewardsFactory;
    private ArenaRegistry arenaRegistry;
    private SignManager signManager;
    private ArenaManager arenaManager;
    private SkullManager skullManager;
    private ReloadManager reloadManager;
    private LeaderboardManager leaderboardManager;
    private boolean initialized;

    @NotNull
    public static WhackMe getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        initializeClasses();

        getLogger().info("Initialization finished.");
        getLogger().info("Join our Discord server: https://discord.gg/uXVU8jmtpU");

        if (this.getOption(ConfigPreferences.Option.UPDATE_NOTIFIER_ENABLED)) {
            UpdateChecker.init(this, 104912).onNewUpdate(result -> getLogger().info("Found a new version available: v" + result.getNewestVersion()));
        }
    }

    @Override
    public void onDisable() {
        this.initialized = false;

        for (Arena arena : arenaRegistry.getArenas()) {
            Player player = arena.getPlayer();

            if (player == null) continue;

            User user = userManager.getUser(player);
            user.addStat(StatisticType.TOURS_PLAYED, 1);
            user.resetAttackCooldown();

            int score = user.getStat(StatisticType.LOCAL_SCORE);

            if (score > user.getStat(StatisticType.RECORD_SCORE)) {
                user.setStat(StatisticType.RECORD_SCORE, score);

                rewardsFactory.performReward(arena, Reward.RewardType.NEW_RECORD);

                player.sendMessage(chatManager.message("in_game.finish_record_message").replace("%points%", Integer.toString(user.getStat(StatisticType.LOCAL_SCORE))));
            } else {
                player.sendMessage(chatManager.message("in_game.finish_message").replace("%points%", Integer.toString(user.getStat(StatisticType.LOCAL_SCORE))));
            }

            if (getOption(ConfigPreferences.Option.CLEAR_INVENTORY)) player.getInventory().clear();
            if (getOption(ConfigPreferences.Option.INVENTORY_MANAGER_ENABLED))
                InventorySerializer.loadInventory(this, player);

            arena.getBossBarManager().removePlayer();
            arena.teleportToEndLocation();
            arena.cleanGameArea();
        }

        userManager.getUserDatabase().shutdown();
    }

    private void initializeClasses() {
        instance = this;

        createFiles();

        configPreferences = new ConfigPreferences(this);
        chatManager = new ChatManager(this);
        commandFramework = new CommandFramework(this);
        commandFramework.addCustomParameter(Player.class, CommandArguments::getSender);
        commandFramework.addCustomParameter(Arena.class, arguments -> arenaRegistry.getArena(arguments.getArgument(0)));
        commandFramework.addCustomParameter("pArena", arguments -> arenaRegistry.getArena(arguments.<Player>getSender()));
        userManager = new UserManager(this);
        soundManager = new SoundManager(this);
        rewardsFactory = new RewardsFactory(this);
        arenaRegistry = new ArenaRegistry(this);
        signManager = new SignManager();
        arenaManager = new ArenaManager(this);
        reloadManager = new ReloadManager(this);
        skullManager = new SkullManager(this);

        if (chatManager.isPapiEnabled()) {
            leaderboardManager = new LeaderboardManager(this);

            new PlaceholderManager(this);
        }

        new GameEvents();
        new PlayerCommands();
        new AdminCommands();

        Metrics metrics = new Metrics(this, 15722);
        metrics.addCustomChart(new SimplePie("database_enabled", () -> getOption(ConfigPreferences.Option.DATABASE_ENABLED) ? "Enabled" : "Disabled"));
        metrics.addCustomChart(new SimplePie("update_notifier", () -> getOption(ConfigPreferences.Option.UPDATE_NOTIFIER_ENABLED) ? "Enabled" : "Disabled"));

        handleAutoDataSaving();
        initialized = true;
    }

    private void createFiles() {
        saveDefaultConfig();

        Stream.of("arenas", "stats", "mysql", "messages", "rewards")
            .map(fileName -> new File(getDataFolder(), fileName + ".yml"))
            .filter(file -> !file.exists())
            .forEach(file -> saveResource(file.getName(), false));
    }

    private void handleAutoDataSaving() {
        long period = getConfig().getLong("Statistic-Saving-Period", 300) * 20;

        if (period > 0) {
            getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {
                userManager.getUserDatabase().saveAllStatistics();

                getLeaderboardManager().ifPresent(LeaderboardManager::updateLeaderboards);
            }, period, period);
        }
    }

    public boolean getOption(ConfigPreferences.Option option) {
        return configPreferences.getOption(option);
    }

    public void callEvent(WMEvent event) {
        this.callEvent(() -> event);
    }

    public void callEvent(Supplier<WMEvent> eventSupplier) {
        if (initialized && isEnabled()) {
            getServer().getScheduler().runTask(this, () -> getServer().getPluginManager().callEvent(eventSupplier.get()));
        }
    }

    @NotNull
    public ChatManager getChatManager() {
        return chatManager;
    }

    @NotNull
    public CommandFramework getCommandFramework() {
        return commandFramework;
    }

    @NotNull
    public ConfigPreferences getConfigPreferences() {
        return configPreferences;
    }

    @NotNull
    public SoundManager getSoundManager() {
        return soundManager;
    }

    @NotNull
    public UserManager getUserManager() {
        return userManager;
    }

    @NotNull
    public RewardsFactory getRewardsFactory() {
        return rewardsFactory;
    }

    @NotNull
    public ArenaRegistry getArenaRegistry() {
        return arenaRegistry;
    }

    @NotNull
    public SignManager getSignManager() {
        return signManager;
    }

    @NotNull
    public ArenaManager getArenaManager() {
        return arenaManager;
    }

    @NotNull
    public SkullManager getSkullManager() {
        return skullManager;
    }

    @NotNull
    public ReloadManager getReloadManager() {
        return reloadManager;
    }

    public Optional<LeaderboardManager> getLeaderboardManager() {
        return Optional.ofNullable(leaderboardManager);
    }
}
