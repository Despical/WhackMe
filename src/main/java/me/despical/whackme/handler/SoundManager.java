package me.despical.whackme.handler;

import me.despical.commons.util.LogUtils;
import me.despical.whackme.ConfigPreferences;
import me.despical.whackme.Main;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2022
 */
public class SoundManager {

	private Sound pointSound, minusPointSound;

	public SoundManager(Main plugin) {
		try {
			this.pointSound = Sound.valueOf(plugin.getConfig().getString(GameSounds.POINT_SOUND.path));
			this.minusPointSound = Sound.valueOf(plugin.getConfig().getString(GameSounds.MINUS_POINT_SOUND.path));
		} catch (Exception ignored) {
			if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.IGNORE_WARNING_MESSAGES)) return;
			LogUtils.sendConsoleMessage("[WhackMe] &cSystem could not load sounds. Look at the config file.");
		}
	}

	public void playSound(Player player, GameSounds sound) {
		if (player == null) return;

		if (sound == GameSounds.POINT_SOUND) {
			if (pointSound != null) player.playSound(player.getLocation(), pointSound, 1F, 2F);
		} else if (minusPointSound != null) player.playSound(player.getLocation(), minusPointSound, 1F, 2F);
	}

	public enum GameSounds {

		POINT_SOUND("Point-Sound"), MINUS_POINT_SOUND("Minus-Point-Sound");

		final String path;

		GameSounds(String path) {
			this.path = path;
		}
	}
}