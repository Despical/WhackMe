package me.despical.whackme.handlers;

import me.despical.commons.compat.XSound;
import me.despical.whackme.WhackMe;
import me.despical.whackme.api.Reloadable;
import org.bukkit.entity.Player;

import java.util.logging.Level;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2022
 */
public class SoundManager implements Reloadable {

	private XSound pointSound, minusPointSound;

	private final WhackMe plugin;

	public SoundManager(WhackMe plugin) {
		this.plugin = plugin;
		this.loadSounds();
	}

	private void loadSounds() {
		try {
			String pointSoundName = plugin.getConfig().getString(GameSound.POINT_SOUND.path);
			String minusPointSoundName = plugin.getConfig().getString(GameSound.MINUS_POINT_SOUND.path);

			this.pointSound = XSound.matchXSound(pointSoundName).orElse(XSound.ENTITY_EXPERIENCE_ORB_PICKUP);
			this.minusPointSound = XSound.matchXSound(minusPointSoundName).orElse(XSound.BLOCK_NOTE_BLOCK_BASS);

			if ("NONE".equals(pointSoundName)) this.pointSound = null;
			if ("NONE".equals(minusPointSoundName)) this.minusPointSound = null;
		} catch (Exception exception) {
			plugin.getLogger().log(Level.WARNING, "System could not load sounds. Check out the config file!", exception);
		}
	}

	public void playSound(Player player, GameSound sound) {
		if (player == null) return;

		if (sound == GameSound.POINT_SOUND) {
			if (pointSound != null) pointSound.play(player.getLocation(), 1F, 2F);
		} else if (minusPointSound != null) minusPointSound.play(player.getLocation(), 1F, 2F);
	}

	@Override
	public void reload() {
		this.loadSounds();
	}

	public enum GameSound {

		POINT_SOUND("Point"),
		MINUS_POINT_SOUND("Minus-Point");

		final String path;

		GameSound(String path) {
			this.path = "Sounds." + path;
		}
	}
}