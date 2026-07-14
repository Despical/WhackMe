package dev.despical.whackme;

import dev.despical.commandframework.CommandArguments;
import dev.despical.commandframework.CommandErrorMessage;
import dev.despical.commandframework.CommandFramework;
import dev.despical.commandframework.options.FrameworkOption;
import dev.despical.commons.util.UpdateChecker;
import dev.despical.fileitems.ItemManager;
import dev.despical.fileitems.ItemOption;
import dev.despical.whackme.api.EventManager;
import dev.despical.whackme.arena.Arena;
import dev.despical.whackme.arena.ArenaDataSaver;
import dev.despical.whackme.arena.ArenaManager;
import dev.despical.whackme.arena.ArenaRegistry;
import dev.despical.whackme.arena.options.ArenaKeys;
import dev.despical.whackme.bossbar.BossBarConfig;
import dev.despical.whackme.chat.ChatManager;
import dev.despical.whackme.command.PlayingCommandPolicy;
import dev.despical.whackme.database.Database;
import dev.despical.whackme.database.FlatFileStorage;
import dev.despical.whackme.database.MySQLStorage;
import dev.despical.whackme.event.GameEvents;
import dev.despical.whackme.event.CommandBlockEvents;
import dev.despical.whackme.game.GameManager;
import dev.despical.whackme.sound.SoundManager;
import dev.despical.whackme.leaderboard.LeaderboardManager;
import dev.despical.whackme.option.BooleanOption;
import dev.despical.whackme.option.ConfigOptions;
import dev.despical.whackme.papi.PlaceholderManager;
import dev.despical.whackme.radio.Radio;
import dev.despical.whackme.radio.impl.EmptyRadio;
import dev.despical.whackme.radio.impl.NBAPIRadio;
import dev.despical.whackme.sign.SignManager;
import dev.despical.whackme.stats.offline.StatsCacheManager;
import dev.despical.whackme.user.User;
import dev.despical.whackme.user.UserManager;
import dev.despical.whackme.util.AutoSaveHandler;
import dev.despical.whackme.util.ShutdownDetector;
import dev.despical.whackme.util.Var;
import lombok.Getter;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * @author Despical
 * <p>
 * Created at 18.06.2022
 */
@Getter
public class WhackMe extends JavaPlugin {

    @Getter
    private static WhackMe instance;

    private ConfigOptions options;
    private ItemManager itemManager;
    private ChatManager chatManager;
    private EventManager eventManager;
    private ArenaRegistry arenaRegistry;
    private Database database;
    private StatsCacheManager statsCacheManager;
    private UserManager userManager;
    private GameManager gameManager;
    private ArenaManager arenaManager;
    private SignManager signManager;
    private LeaderboardManager leaderboardManager;
    private CommandFramework commandFramework;
    private PlayingCommandPolicy playingCommandPolicy;
    private BossBarConfig bossBarConfig;
    private SoundManager soundManager;
    private Radio radio;
    private ArenaDataSaver arenaDataSaver;
    private Metrics metrics;

    @Override
    public void onEnable() {
        ShutdownDetector.init();

        instance = this;

        createConfigFiles();
        initializeClasses();
    }

    @Override
    public void onDisable() {
        arenaDataSaver.saveAllArenas();
        arenaManager.handleDisable();
        database.shutdown();
        metrics.shutdown();
    }

    private void createConfigFiles() {
        saveDefaultConfig();
        saveResourceIfMissing("mysql.yml");
        seedDefaultSongsOnce();
    }

    private void initializeClasses() {
        this.loadItemManager();

        options = new ConfigOptions(this);
        chatManager = new ChatManager(this);
        eventManager = new EventManager(this);
        bossBarConfig = new BossBarConfig(this);
        arenaRegistry = new ArenaRegistry(this);
        database = createDatabase();
        statsCacheManager = new StatsCacheManager(this);
        userManager = new UserManager(this);
        gameManager = new GameManager(this);
        playingCommandPolicy = new PlayingCommandPolicy(this);
        soundManager = new SoundManager(this);
        signManager = new SignManager(this);
        arenaManager = new ArenaManager(this);
        leaderboardManager = new LeaderboardManager(this);
        radio = createRadio();
        arenaDataSaver = new ArenaDataSaver(this);

        validateArenasMissingSongs();

        registerCommands();
        registerEvents();
        registerPlaceholderManager();
        runAutoSave();
        initializeMetrics();
        checkUpdates();
    }

    private void loadItemManager() {
        itemManager = new ItemManager(this, _ -> ItemOption.enableOptions(ItemOption.GLOW, ItemOption.AMOUNT));

        registerItems();
    }

    public void registerItems() {
        itemManager.registerItems("menu/setup-menu", "items");
        itemManager.registerItems("stats-menu-items", "items", "menu/stats-menu");
    }

    private Database createDatabase() {
        if (options.isEnabled(BooleanOption.DATABASE_ENABLED)) {
            return new MySQLStorage();
        }

        return new FlatFileStorage();
    }

    private Radio createRadio() {
        if (!getServer().getPluginManager().isPluginEnabled("NoteBlockAPI")) {
            return new EmptyRadio();
        }

        return new NBAPIRadio(this);
    }

    private void runAutoSave() {
        new AutoSaveHandler(this).runTaskTimerAsynchronously(this, 20, 20 * 60 * 5);
    }

    private void registerCommands() {
        commandFramework = new CommandFramework(this);

        if (BooleanOption.DEBUG.value()) {
            commandFramework.options().enableOptions(FrameworkOption.DEBUG);
        }

        commandFramework.addCustomParameter(Player.class, CommandArguments::getSender);
        commandFramework.addCustomParameter(User.class, args -> userManager.getUser(args.<Player>getSender()));
        commandFramework.addCustomParameter(Arena.class, args -> arenaRegistry.getArena(args.getFirst()));
        commandFramework.registerAllInPackage("dev.despical.whackme.command");

        var messages = Stream.of(CommandErrorMessage.SHORT_ARG_SIZE, CommandErrorMessage.LONG_ARG_SIZE);
        messages.forEach(message -> message.setHandler((cmd, args) -> {
            chatManager.sendMessage(args, "correct-usage",
                Var.of("%usage%", cmd.usage().replace("%label%", args.getLabel())));
            return true;
        }));
    }

    private void registerEvents() {
        new GameEvents();
        new CommandBlockEvents();
    }

    private void checkUpdates() {
        if (!BooleanOption.UPDATE_NOTIFIER.value()) {
            return;
        }

        UpdateChecker.init(this, 133887).onNewUpdate(_ -> {
            Logger logger = getLogger();
            logger.log(Level.INFO, "An update for Whack Me ({0}) is available at:", getDescription().getVersion());
            logger.log(Level.INFO, "https://www.spigotmc.org/resources/whack-me.104912/");
        });
    }

    private void registerPlaceholderManager() {
        if (!getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            return;
        }

        PlaceholderManager manager = new PlaceholderManager(this);
        manager.register();
    }

    private void initializeMetrics() {
        metrics = new Metrics(this, 15722);
        metrics.addCustomChart(new SimplePie("database_enabled", () -> options.isEnabled(BooleanOption.DATABASE_ENABLED) ? "Enabled" : "Disabled"));
        metrics.addCustomChart(new SimplePie("update_notifier", () -> options.isEnabled(BooleanOption.UPDATE_NOTIFIER) ? "Enabled" : "Disabled"));
        metrics.addCustomChart(new SimplePie("noteblockapi_enabled", () -> isPluginEnabled("NoteBlockAPI") ? "yes" : "no"));
    }

    private boolean isPluginEnabled(String pluginName) {
        return getServer().getPluginManager().isPluginEnabled(pluginName);
    }

    private void saveResourceIfMissing(String resourcePath) {
        File targetFile = new File(getDataFolder(), resourcePath);

        if (targetFile.exists()) {
            return;
        }

        saveResource(resourcePath, false);
    }

    private void seedDefaultSongsOnce() {
        File musicsFolder = new File(getDataFolder(), "musics");
        if (!musicsFolder.exists() && !musicsFolder.mkdirs()) {
            getLogger().warning("Failed to create musics folder for default song seed marker.");
            return;
        }

        File markerFile = new File(musicsFolder, ".default-songs-seeded");
        if (markerFile.exists()) {
            return;
        }

        int copiedSongs = 0;

        for (String resourcePath : getBundledDefaultSongs()) {
            File targetFile = new File(getDataFolder(), resourcePath);

            if (targetFile.exists()) {
                continue;
            }

            saveResource(resourcePath, false);
            copiedSongs++;
        }

        try {
            Files.writeString(markerFile.toPath(), "seeded=" + copiedSongs, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            getLogger().warning("Failed to create default song seed marker: " + exception.getMessage());
        }
    }

    private List<String> getBundledDefaultSongs() {
        try (InputStream inputStream = getResource("musics/default-songs.txt")) {
            if (inputStream == null) {
                return List.of();
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                return reader.lines()
                    .map(String::trim)
                    .filter(line -> !line.isEmpty())
                    .map(fileName -> "musics/" + fileName)
                    .toList();
            }
        } catch (IOException exception) {
            getLogger().warning("Failed to read bundled default songs list: " + exception.getMessage());
            return List.of();
        }
    }

    private void validateArenasMissingSongs() {
        if (!(radio instanceof NBAPIRadio nbApi)) {
            return;
        }

        Map<String, Set<String>> missingSongsByArena = new LinkedHashMap<>();

        for (Arena arena : arenaRegistry.getArenas()) {
            String arenaSong = arena.getOption(ArenaKeys.ARENA_SONG);

            if (arenaSong != null && !arenaSong.isEmpty() && nbApi.getSong(arenaSong) == null) {
                missingSongsByArena.computeIfAbsent(arena.getId(), _ -> new LinkedHashSet<>()).add(arenaSong);
                arena.setOption(ArenaKeys.ARENA_SONG, null);
            }
        }

        if (missingSongsByArena.isEmpty()) {
            return;
        }

        getLogger().warning("Missing songs detected for some arenas. Those arenas were reset to no music.");
        missingSongsByArena.forEach((arenaId, missingSongs) -> {
            getLogger().warning("Arena: " + arenaId);
            missingSongs.forEach(song -> getLogger().warning(" - Missing song: " + song));
        });
    }
}
