package dev.despical.whackme.arena;

import dev.despical.whackme.WhackMe;
import dev.despical.whackme.arena.options.ArenaKeys;
import dev.despical.whackme.arena.options.ArenaOption;
import dev.despical.whackme.game.Game;
import dev.despical.whackme.user.User;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Despical
 * <p>
 * Created at 18.06.2022
 */
@Getter
public class Arena {

    private static final WhackMe plugin = WhackMe.getInstance();

    private Game game;

    private final String id;
    private final Map<ArenaOption<?>, Object> options;

    public Arena(String id) {
        this.id = id;
        this.options = new HashMap<>();
        this.registerDefaultOptions();
    }

    public <T> T getOption(ArenaOption<T> option) {
        Object value = options.computeIfAbsent(option, _ -> option.getDefaultValue());
        return option.getType().cast(value);
    }

    public <T> void setOption(ArenaOption<T> option, T value) {
        options.put(option, value);
    }

    public String getPlayerName() {
        Player player = getPlayer();
        return player == null ? plugin.getChatManager().getRawString("unknown-player") : player.getName();
    }

    public boolean isPlaying(Player player) {
        return game != null && game.isPlaying(player);
    }

    public boolean isPlaying(User user) {
        return game != null && game.isPlaying(user);
    }

    public Player getPlayer() {
        return game == null ? null : game.getPlayer();
    }

    public boolean isGameNonnull() {
        return game != null;
    }

    public void start() {
        if (game != null) {
            return;
        }

        game = new Game(this);
        game.start();
    }

    public void stop() {
        if (game == null) {
            return;
        }

        game.shutdown();
        game = null;
    }

    private void registerDefaultOptions() {
        for (ArenaOption<?> setting : ArenaKeys.getAllKeys()) {
            options.put(setting, setting.getDefaultValue());
        }
    }

    @Override
    public String toString() {
        return "Arena[id=%s, game=%s]".formatted(id, String.valueOf(game));
    }
}
