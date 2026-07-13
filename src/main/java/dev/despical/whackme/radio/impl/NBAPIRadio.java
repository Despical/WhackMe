package dev.despical.whackme.radio.impl;

import com.xxmicloxx.NoteBlockAPI.model.RepeatMode;
import com.xxmicloxx.NoteBlockAPI.model.Song;
import com.xxmicloxx.NoteBlockAPI.songplayer.RadioSongPlayer;
import com.xxmicloxx.NoteBlockAPI.utils.NBSDecoder;
import dev.despical.whackme.WhackMe;
import dev.despical.whackme.arena.Arena;
import dev.despical.whackme.arena.options.ArenaKeys;
import dev.despical.whackme.radio.Radio;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

/**
 * @author Despical
 * <p>
 * Created at 13.07.2026
 */
public class NBAPIRadio implements Radio {

    private final WhackMe plugin;
    @Getter
    private final Map<String, Song> songs;
    private final Map<Arena, Map<Player, RadioSongPlayer>> arenaRadios;
    private final Map<UUID, RadioSongPlayer> previewRadios;

    public NBAPIRadio(WhackMe plugin) {
        this.plugin = plugin;
        this.songs = new HashMap<>();
        this.arenaRadios = new HashMap<>();
        this.previewRadios = new HashMap<>();
        this.loadSongs();
    }

    private void loadSongs() {
        File songsFolder = new File(plugin.getDataFolder(), "musics");

        if (!songsFolder.exists()) {
            songsFolder.mkdirs();
            plugin.getLogger().info("Created musics/ folder for NBS files.");
        }

        try {
            Path songsPath = songsFolder.toPath();
            List<String> loadedSongNames = new ArrayList<>();
            List<String> failedSongNames = new ArrayList<>();

            Files.walk(songsPath, 1)
                .filter(path -> path.toFile().isFile())
                .filter(path -> path.toString().endsWith(".nbs"))
                .sorted()
                .forEach(path -> {
                    String fileName = path.getFileName().toString();
                    String songName = fileName.replace(".nbs", "");

                    try {
                        Song song = NBSDecoder.parse(path.toFile());
                        songs.put(songName, song);
                        loadedSongNames.add(songName);
                    } catch (Exception exception) {
                        failedSongNames.add(songName);
                        plugin.getLogger().log(Level.WARNING, "Failed to load song: " + songName, exception);
                    }
                });

            if (!loadedSongNames.isEmpty()) {
                plugin.getLogger().info("Loaded songs: " + loadedSongNames.size());
            }

            if (!failedSongNames.isEmpty()) {
                plugin.getLogger().warning("Failed songs: " + String.join(", ", failedSongNames));
            }

            if (songs.isEmpty()) {
                plugin.getLogger().warning("No NBS song files found in musics/ folder.");
            }
        } catch (IOException exception) {
            plugin.getLogger().log(Level.WARNING, "Failed to load songs folder", exception);
        }
    }

    public Song getSong(String songName) {
        return songs.get(songName);
    }

    public List<String> getAvailableSongs() {
        return songs.keySet().stream().sorted(String.CASE_INSENSITIVE_ORDER).toList();
    }

    @Override
    public void addArena(Arena arena) {
        Player player = arena.getPlayer();

        if (player != null) {
            addPlayer(arena, player);
        }
    }

    @Override
    public void removeArena(Arena arena) {
        Map<Player, RadioSongPlayer> playerRadios = arenaRadios.remove(arena);

        if (playerRadios == null) {
            return;
        }

        for (RadioSongPlayer radio : playerRadios.values()) {
            radio.setPlaying(false);
        }
    }

    @Override
    public void addPlayer(Arena arena, Player player) {
        Map<Player, RadioSongPlayer> playerRadios = arenaRadios.computeIfAbsent(arena, key -> new HashMap<>());
        
        if (playerRadios.containsKey(player)) {
            return;
        }

        String arenaSong = arena.getOption(ArenaKeys.ARENA_SONG);
        Song songToPlay = null;

        if (arenaSong != null && !arenaSong.isEmpty()) {
            songToPlay = getSong(arenaSong);
        }

        if (songToPlay == null) {
            songToPlay = getDefaultSong();
        }

        if (songToPlay == null) {
            return;
        }

        RadioSongPlayer radio = new RadioSongPlayer(songToPlay);
        radio.setRepeatMode(RepeatMode.ONE);
        radio.addPlayer(player);
        radio.playNextSong();
        radio.setPlaying(true);

        playerRadios.put(player, radio);
    }

    @Override
    public void removePlayer(Arena arena, Player player) {
        Map<Player, RadioSongPlayer> playerRadios = arenaRadios.get(arena);

        if (playerRadios != null) {
            RadioSongPlayer radio = playerRadios.remove(player);

            if (radio != null) {
                radio.removePlayer(player);
                radio.setPlaying(false);
            }
        }

        stopPreview(player);
    }

    public boolean previewSong(Player player, String songName) {
        Song song = getSong(songName);
        if (song == null) {
            return false;
        }

        stopPreview(player);

        RadioSongPlayer previewPlayer = new RadioSongPlayer(song);
        previewPlayer.setRepeatMode(RepeatMode.ONE);
        previewPlayer.addPlayer(player);
        previewPlayer.playNextSong();
        previewPlayer.setPlaying(true);

        previewRadios.put(player.getUniqueId(), previewPlayer);
        return true;
    }

    public void stopPreview(Player player) {
        RadioSongPlayer previewPlayer = previewRadios.remove(player.getUniqueId());
        if (previewPlayer == null) {
            return;
        }

        previewPlayer.removePlayer(player);
        previewPlayer.setPlaying(false);
    }

    private Song getDefaultSong() {
        return songs.values().stream().findFirst().orElse(null);
    }
}
