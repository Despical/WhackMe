package dev.despical.whackme.sign;

import dev.despical.commons.configuration.ConfigUtils;
import dev.despical.commons.serializer.LocationSerializer;
import dev.despical.whackme.WhackMe;
import dev.despical.whackme.arena.Arena;
import dev.despical.whackme.arena.ArenaRegistry;
import dev.despical.whackme.chat.ChatManager;
import dev.despical.whackme.game.Game;
import dev.despical.whackme.game.GameState;
import dev.despical.whackme.util.Utils;
import dev.despical.whackme.util.Var;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Rotatable;
import org.bukkit.block.sign.Side;
import org.bukkit.block.sign.SignSide;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.command.CommandSender;

import java.util.*;

/**
 * @author Despical
 * <p>
 * Created at 12.12.2025
 */
public class SignManager {

    private FileConfiguration config;
    private ArenaSignEvents arenaSignEvents;
    private List<String> signLines;

    private final WhackMe plugin;
    private final ChatManager chatManager;
    private final Map<BlockKey, ArenaSign> signsByBlock;
    private final Map<Arena, Set<ArenaSign>> signsByArena;
    private final Map<Arena, List<String>> unresolvedSignLocations;
    private final Map<GameState, String> gameStateToString;

    public SignManager(WhackMe plugin) {
        this.plugin = plugin;
        this.chatManager = plugin.getChatManager();
        this.signsByBlock = new HashMap<>();
        this.signsByArena = new HashMap<>();
        this.unresolvedSignLocations = new HashMap<>();
        this.signLines = List.of();
        this.gameStateToString = new EnumMap<>(GameState.class);
        this.loadSigns();
    }

    private void loadSigns() {
        this.loadConfig();

        ArenaRegistry arenaRegistry = plugin.getArenaRegistry();
        FileConfiguration config = arenaRegistry.getConfig();

        boolean updateConfig = false;

        for (Arena arena : arenaRegistry.getArenas()) {
            String path = arena.getId() + ".signs";

            List<String> locations = config.getStringList(path);
            Iterator<String> iterator = locations.iterator();

            while (iterator.hasNext()) {
                String serializedLocation = iterator.next();
                Location location;

                try {
                    location = LocationSerializer.fromString(serializedLocation);
                } catch (RuntimeException _) {
                    location = null;
                }

                if (location == null) {
                    unresolvedSignLocations.computeIfAbsent(arena, _ -> new ArrayList<>()).add(serializedLocation);

                    plugin.getLogger().warning("Could not load sign for arena '" + arena.getId()
                        + "': world is missing or location is invalid. Keeping the saved entry.");
                    continue;
                }

                Block block = location.getBlock();

                if (block.getState() instanceof Sign) {
                    ArenaSign arenaSign = new ArenaSign(arena, block);

                    trackArenaSign(arenaSign);
                    updateSign(arenaSign);
                    continue;
                }

                iterator.remove();
                updateConfig = true;
            }

            if (updateConfig) {
                config.set(path, locations);
                updateConfig = false;
            }
        }

        refreshListenerRegistration();
    }

    public void reload() {
        this.loadConfig();
        this.signsByArena.keySet().forEach(this::updateSigns);
    }

    public void sendMessage(CommandSender recipient, String path, Var... vars) {
        recipient.sendMessage(getMessageComponent(path, vars));
    }

    public void updateSigns(Arena arena) {
        getSigns(arena).forEach(this::updateSign);
    }

    private void updateSign(ArenaSign arenaSign) {
        Sign sign = arenaSign.sign();

        Arena arena = arenaSign.arena();
        Game game = arena.getGame();

        String state = game == null
            ? gameStateToString.get(GameState.INACTIVE)
            : gameStateToString.get(game.getState());

        SignSide side = sign.getSide(Side.FRONT);
        boolean changed = false;

        for (int i = 0; i < 4; i++) {
            String configuredLine = i < signLines.size() ? signLines.get(i) : "";
            Component line = formatSign(configuredLine, state, arena);

            if (Objects.equals(side.line(i), line)) {
                continue;
            }

            side.line(i, line);
            changed = true;
        }

        if (!sign.isWaxed()) {
            sign.setWaxed(true);
            changed = true;
        }

        if (changed) {
            sign.update();
        }
    }

    public void addArenaSign(Arena arena, Block block) {
        if (arena == null || block == null || !(block.getState() instanceof Sign)) {
            return;
        }

        ArenaSign arenaSign = new ArenaSign(arena, block);
        ArenaSign existing = getArenaSignByBlock(block);

        if (existing != null) {
            untrackArenaSign(existing);
        }

        trackArenaSign(arenaSign);
        refreshListenerRegistration();

        updateSign(arenaSign);
    }

    public void removeArenaSign(ArenaSign arenaSign) {
        if (arenaSign == null) {
            return;
        }

        untrackArenaSign(arenaSign);
        refreshListenerRegistration();
    }

    public void removeArenaSigns(Arena arena) {
        Set<ArenaSign> arenaSigns = signsByArena.remove(arena);
        if (arenaSigns == null || arenaSigns.isEmpty()) {
            refreshListenerRegistration();
            return;
        }

        for (ArenaSign arenaSign : arenaSigns) {
            signsByBlock.remove(BlockKey.of(arenaSign.block()));
        }

        refreshListenerRegistration();
    }

    public List<ArenaSign> getSigns(Arena arena) {
        Set<ArenaSign> arenaSigns = signsByArena.get(arena);
        return arenaSigns == null ? List.of() : List.copyOf(arenaSigns);
    }

    public List<String> getSerializedLocations(Arena arena) {
        List<String> locations = new ArrayList<>(getSigns(arena).stream()
            .map(ArenaSign::serializedLocation)
            .toList());

        locations.addAll(unresolvedSignLocations.getOrDefault(arena, List.of()));
        return locations;
    }

    public Var[] getSignVars(Block block) {
        Location location = block.getLocation();

        return new Var[]{
            Var.of("%x%", location.getBlockX()),
            Var.of("%y%", location.getBlockY()),
            Var.of("%z%", location.getBlockZ()),
            Var.of("%direction%", resolveSignDirection(block.getBlockData())),
        };
    }

    public boolean isArenaSign(Block block) {
        return getArenaSignByBlock(block) != null;
    }

    private String resolveSignDirection(BlockData blockData) {
        if (blockData instanceof Directional directional) {
            return directional.getFacing().name();
        }

        if (blockData instanceof Rotatable rotatable) {
            return rotatable.getRotation().name();
        }

        return "UNKNOWN";
    }

    private Component formatSign(String line, String state, Arena arena) {
        return chatManager.parseMessage(Utils.format(line,
            Var.of("%arena%", arena.getId()),
            Var.of("%state%", state)
        ));
    }

    ArenaSign getArenaSignByBlock(Block block) {
        BlockKey blockKey = BlockKey.of(block);
        if (blockKey == null) {
            return null;
        }

        ArenaSign arenaSign = signsByBlock.get(blockKey);
        if (arenaSign == null) {
            return null;
        }

        if (block.getState() instanceof Sign) {
            return arenaSign;
        }

        untrackArenaSign(arenaSign);
        refreshListenerRegistration();
        return null;
    }

    private void trackArenaSign(ArenaSign arenaSign) {
        signsByBlock.put(BlockKey.of(arenaSign.block()), arenaSign);
        signsByArena.computeIfAbsent(arenaSign.arena(), _ -> new HashSet<>()).add(arenaSign);
    }

    private void untrackArenaSign(ArenaSign arenaSign) {
        signsByBlock.remove(BlockKey.of(arenaSign.block()));

        Set<ArenaSign> arenaSigns = signsByArena.get(arenaSign.arena());
        if (arenaSigns == null) {
            return;
        }

        arenaSigns.remove(arenaSign);

        if (arenaSigns.isEmpty()) {
            signsByArena.remove(arenaSign.arena());
        }
    }

    private void refreshListenerRegistration() {
        if (signsByBlock.isEmpty()) {
            unregisterArenaSignEvents();
            return;
        }

        registerArenaSignEvents();
    }

    private void registerArenaSignEvents() {
        if (arenaSignEvents != null) {
            return;
        }

        arenaSignEvents = new ArenaSignEvents(this);
    }

    private void unregisterArenaSignEvents() {
        if (arenaSignEvents == null) {
            return;
        }

        HandlerList.unregisterAll(arenaSignEvents);
        arenaSignEvents = null;
    }

    private void loadConfig() {
        config = ConfigUtils.getConfig(plugin, "signs");
        signLines = config.getStringList("lines");

        for (GameState state : GameState.values()) {
            gameStateToString.put(state, getRawString("game-states." + state.getPath()));
        }
    }

    private Component getMessageComponent(String path, Var... vars) {
        return chatManager.parseMessage(getRawString(path), vars);
    }

    private String getRawString(String path) {
        return config.getString(path, "");
    }

    private record BlockKey(UUID worldId, int x, int y, int z) {

        private static BlockKey of(Block block) {
            if (block == null) {
                return null;
            }

            return new BlockKey(block.getWorld().getUID(), block.getX(), block.getY(), block.getZ());
        }
    }
}
