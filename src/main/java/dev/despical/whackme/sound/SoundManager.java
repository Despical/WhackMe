package dev.despical.whackme.sound;

import dev.despical.commons.configuration.ConfigUtils;
import dev.despical.whackme.WhackMe;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.EnumMap;
import java.util.Map;

/**
 * @author Despical
 * <p>
 * Created at 13.07.2026
 */
public class SoundManager {

    private final WhackMe plugin;
    private final Map<GameSound, SoundData> sounds = new EnumMap<>(GameSound.class);

    public SoundManager(WhackMe plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        sounds.clear();
        FileConfiguration config = ConfigUtils.getConfig(plugin, "sounds");

        for (GameSound gameSound : GameSound.values()) {
            String path = "sounds." + gameSound.getPath() + ".";
            if (!config.getBoolean(path + "enabled", true)) {
                continue;
            }

            String name = config.getString(path + "sound");
            Sound sound = SoundResolver.resolve(name);
            if (sound == null) {
                plugin.getLogger().warning("Could not register sound '" + gameSound.getPath() + "': invalid sound '" + name + "'.");
                continue;
            }

            sounds.put(gameSound, new SoundData(
                sound,
                (float) config.getDouble(path + "volume", 1D),
                (float) config.getDouble(path + "pitch", 1D)
            ));
        }
    }

    public void play(Player player, GameSound gameSound) {
        SoundData data = sounds.get(gameSound);
        if (player != null && data != null) {
            player.playSound(player.getLocation(), data.sound(), data.volume(), data.pitch());
        }
    }

    /**
     * @author Despical
     * <p>
     * Created at 13.07.2026
     */
    private record SoundData(Sound sound, float volume, float pitch) {
    }
}
