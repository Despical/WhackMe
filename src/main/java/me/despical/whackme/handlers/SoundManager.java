package me.despical.whackme.handlers;

import me.despical.commons.compat.XSound;
import me.despical.whackme.Main;
import org.bukkit.entity.Player;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2022
 */
public class SoundManager {

	private XSound pointSound, minusPointSound;

	public SoundManager(Main plugin) {
		try {
			this.pointSound = XSound.matchXSound(plugin.getConfig().getString(GameSounds.POINT_SOUND.path)).orElse(XSound.ENTITY_EXPERIENCE_BOTTLE_THROW);
			this.minusPointSound = XSound.matchXSound(plugin.getConfig().getString(GameSounds.MINUS_POINT_SOUND.path)).orElse(XSound.BLOCK_NOTE_BLOCK_BASS);
		} catch (Exception ignored) {
			plugin.getLogger().warning("System could not load sounds. Check out the config file.");
		}
	}

	public void playSound(Player player, GameSounds sound) {
		if (player == null) return;

		if (sound == GameSounds.POINT_SOUND) {
			if (pointSound != null) pointSound.play(player.getLocation(), 1F, 2F);
		} else if (minusPointSound != null) minusPointSound.play(player.getLocation(), 1F, 2F);
	}

	public enum GameSounds {

		POINT_SOUND("Point-Sound"), MINUS_POINT_SOUND("Minus-Point-Sound");

		final String path;

		GameSounds(String path) {
			this.path = path;
		}
	}
}