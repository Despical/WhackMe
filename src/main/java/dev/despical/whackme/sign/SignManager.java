package dev.despical.whackme.sign;

import dev.despical.commons.configuration.ConfigUtils;
import dev.despical.commons.serializer.LocationSerializer;
import dev.despical.whackme.WhackMe;
import dev.despical.whackme.arena.Arena;
import dev.despical.whackme.arena.options.ArenaKeys;
import dev.despical.whackme.chat.ChatManager;
import dev.despical.whackme.game.GameState;
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

    private ArenaSignEvents signEvents;
    private FileConfiguration signConfig;
    private List<Component> signLines;

    private final WhackMe plugin;
    private final ChatManager chatManager;
    private final Map<GameState, String> stateNames;
    private final Map<SignBlockKey, ArenaSign> signsByBlock;
    private final Map<Arena, Set<ArenaSign>> signsByArena;

    public SignManager(WhackMe plugin) {
        this.plugin = plugin;
        this.chatManager = plugin.getChatManager();
        this.signsByBlock = new HashMap<>();
        this.signsByArena = new HashMap<>();
        this.signLines = List.of();
        this.stateNames = new EnumMap<>(GameState.class);
        this.loadSigns();
    }

    private void loadSigns() {
        signConfig = ConfigUtils.getConfig(plugin, "signs");
        signLines = signConfig.getStringList("lines").stream().map(chatManager::parseMessage).toList();
        stateNames.clear();

        for (GameState state : GameState.values()) {
            stateNames.put(state, signConfig.getString("game-states." + state.getPath(), state.getPath()));
        }

        signsByBlock.clear();
        signsByArena.clear();

        FileConfiguration config = plugin.getArenaRegistry().getConfig();
        boolean updateConfig = false;

        for (String arenaId : config.getKeys(false)) {
            List<String> locations = config.getStringList(arenaId + ".signs");
            Iterator<String> iterator = locations.iterator();

            while (iterator.hasNext()) {
                Block block = LocationSerializer.fromString(iterator.next()).getBlock();

                if (block.getState() instanceof Sign) {
                    addArenaSign(plugin.getArenaRegistry().getArena(arenaId), block);
                    continue;
                }

                iterator.remove();
                updateConfig = true;
            }

            if (updateConfig) {
                config.set(arenaId + ".signs", locations);
                updateConfig = false;
            }
        }

        updateListenerRegistration();
    }

    public void reload() {
        this.loadSigns();
    }

    public void updateSigns(Arena arena) {
        getSigns(arena).forEach(this::updateSign);
    }

    public void sendMessage(CommandSender recipient, String path, Var... vars) {
        recipient.sendMessage(chatManager.parseMessage(signConfig.getString(path, ""), vars));
    }

    private void updateSign(ArenaSign arenaSign) {
        Sign sign = arenaSign.sign();
        SignSide side = sign.getSide(Side.FRONT);

        for (int i = 0; i < signLines.size(); i++) {
            side.line(i, formatSign(signLines.get(i), arenaSign.arena()));
        }

        sign.setWaxed(true);
        sign.update();
    }

    public void addArenaSign(Arena arena, Block block) {
        if (arena == null || !(block.getState() instanceof Sign)) {
            return;
        }

        SignBlockKey blockKey = SignBlockKey.from(block);
        ArenaSign existingSign = signsByBlock.remove(blockKey);

        if (existingSign != null) {
            untrack(existingSign);
        }

        ArenaSign arenaSign = new ArenaSign(arena, block);
        track(arenaSign);

        ensureListenerRegistered();
        updateSign(arenaSign);
    }

    public boolean isArenaSign(Block block) {
        return getArenaSignByBlock(block) != null;
    }

    public void removeArenaSign(ArenaSign arenaSign) {
        untrack(arenaSign);
        updateListenerRegistration();
    }

    public void removeArenaSigns(Arena arena) {
        Set<ArenaSign> signs = signsByArena.remove(arena);
        if (signs != null) {
            signs.forEach(sign -> signsByBlock.remove(SignBlockKey.from(sign.block())));
        }

        updateListenerRegistration();
    }

    public List<ArenaSign> getSigns(Arena arena) {
        Set<ArenaSign> signs = signsByArena.get(arena);
        return signs == null ? List.of() : List.copyOf(signs);
    }

    public Var[] getSignVars(Block block) {
        Location location = block.getLocation();
        String direction = resolveSignDirection(block.getBlockData());

        return new Var[]{
            Var.of("%x%", location.getBlockX()),
            Var.of("%y%", location.getBlockY()),
            Var.of("%z%", location.getBlockZ()),
            Var.of("%direction%", direction),
        };
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

    private Component formatSign(Component component, Arena arena) {
        return chatManager.replaceVarsInComponent(component,
            Var.of("%arena%", arena.getId()),
            Var.of("%state%", formatArenaState(arena))
        );
    }

    private String formatArenaState(Arena arena) {
        if (!arena.getOption(ArenaKeys.READY) || arena.getGame() == null) {
            return stateNames.get(GameState.INACTIVE);
        }

        var state = arena.getGame().getState();
        return stateNames.getOrDefault(state, state.getPath());
    }

    ArenaSign getArenaSignByBlock(Block block) {
        if (block == null) {
            return null;
        }

        ArenaSign sign = signsByBlock.get(SignBlockKey.from(block));
        if (sign == null || block.getState() instanceof Sign) {
            return sign;
        }

        untrack(sign);
        updateListenerRegistration();
        return null;
    }

    private void ensureListenerRegistered() {
        if (signEvents != null) {
            return;
        }

        signEvents = new ArenaSignEvents(plugin, this);
        plugin.getServer().getPluginManager().registerEvents(signEvents, plugin);
    }

    private void updateListenerRegistration() {
        if (!signsByBlock.isEmpty()) {
            ensureListenerRegistered();
            return;
        }

        if (signEvents != null) {
            HandlerList.unregisterAll(signEvents);
            signEvents = null;
        }
    }

    private void track(ArenaSign sign) {
        signsByBlock.put(SignBlockKey.from(sign.block()), sign);
        signsByArena.computeIfAbsent(sign.arena(), _ -> new HashSet<>()).add(sign);
    }

    private void untrack(ArenaSign sign) {
        signsByBlock.remove(SignBlockKey.from(sign.block()));
        Set<ArenaSign> signs = signsByArena.get(sign.arena());

        if (signs == null) {
            return;
        }

        signs.remove(sign);

        if (signs.isEmpty()) {
            signsByArena.remove(sign.arena());
        }
    }

    /**
     * @author Despical
     * <p>
     * Created at 12.12.2025
     */
    private record SignBlockKey(String worldName, int x, int y, int z) {

        private static SignBlockKey from(Block block) {
            Location location = block.getLocation();
            return new SignBlockKey(
                location.getWorld().getName(),
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ()
            );
        }
    }
}
