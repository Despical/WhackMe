package me.despical.whackme;

import me.despical.commandframework.CommandFramework;
import me.despical.commons.serializer.InventorySerializer;
import me.despical.commons.util.Collections;
import me.despical.commons.util.UpdateChecker;
import me.despical.whackme.api.event.WMEvent;
import me.despical.whackme.api.statistics.StatisticType;
import me.despical.whackme.arena.Arena;
import me.despical.whackme.arena.ArenaRegistry;
import me.despical.whackme.arena.managers.ArenaManager;
import me.despical.whackme.command.AdminCommands;
import me.despical.whackme.command.PlayerCommands;
import me.despical.whackme.event.GameEvents;
import me.despical.whackme.handlers.ChatManager;
import me.despical.whackme.handlers.PlaceholderManager;
import me.despical.whackme.handlers.ReloadManager;
import me.despical.whackme.handlers.SoundManager;
import me.despical.whackme.handlers.rewards.Reward;
import me.despical.whackme.handlers.rewards.RewardsFactory;
import me.despical.whackme.handlers.sign.SignManager;
import me.despical.whackme.leaderboard.LeaderboardManager;
import me.despical.whackme.skulls.SkullManager;
import me.despical.whackme.user.User;
import me.despical.whackme.user.UserManager;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Optional;
import java.util.function.Supplier;

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

        UpdateChecker.setEnabled(this.getOption(ConfigPreferences.Option.UPDATE_NOTIFIER_ENABLED));
        UpdateChecker.init(this, 104912).onNewUpdate(result -> getLogger().info("Found a new version available: v" + result.getNewestVersion()));

        getLogger().info("Initialization finished.");
        getLogger().info("Join our Discord server: https://discord.gg/uXVU8jmtpU");
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

                rewardsFactory.performReward(player, Reward.RewardType.NEW_RECORD);

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

        this.setupConfigurationFiles();

        this.configPreferences = new ConfigPreferences(this);
        this.chatManager = new ChatManager(this);
        this.commandFramework = new CommandFramework(this);
        this.userManager = new UserManager(this);
        this.soundManager = new SoundManager(this);
        this.rewardsFactory = new RewardsFactory(this);
        this.arenaRegistry = new ArenaRegistry(this);
        this.signManager = new SignManager();
        this.arenaManager = new ArenaManager(this);
        this.reloadManager = new ReloadManager(this);
        this.skullManager = new SkullManager(this);

        if (chatManager.isPapiEnabled()) {
            this.leaderboardManager = new LeaderboardManager(this);

            new PlaceholderManager(this);
        }

        new GameEvents();
        new PlayerCommands();
        new AdminCommands();

        User.cooldownHandlerTask();

        Metrics metrics = new Metrics(this, 15722);
        metrics.addCustomChart(new SimplePie("database_enabled", () -> getOption(ConfigPreferences.Option.DATABASE_ENABLED) ? "Enabled" : "Disabled"));
        metrics.addCustomChart(new SimplePie("update_notifier", () -> getOption(ConfigPreferences.Option.UPDATE_NOTIFIER_ENABLED) ? "Enabled" : "Disabled"));

        this.handleAutoDataSaving();
        this.initialized = true;
    }

    private void setupConfigurationFiles() {
        saveDefaultConfig();

        Collections.streamOf("arenas", "stats", "mysql", "messages", "rewards").filter(name -> !new File(getDataFolder(), name + ".yml").exists()).forEach(name -> saveResource(name + ".yml", false));
    }

    private void handleAutoDataSaving() {
        long period = getConfig().getLong("Statistic-Saving-Period", 300);

        if (period > 0) {
            getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {
                userManager.getUserDatabase().saveAllStatistics();

                Optional.ofNullable(leaderboardManager).ifPresent(LeaderboardManager::updateLeaderboards);
            }, period, period * 20);
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

    public LeaderboardManager getLeaderboardManager() {
        return leaderboardManager;
    }
}