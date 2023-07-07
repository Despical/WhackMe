package me.despical.whackme.arena.options;

import me.despical.whackme.Main;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * @author Despical
 * <p>
 * Created at 20.06.2022
 */
public enum ArenaOption {

	TIMER("Gameplay-Time", 30),

	MINIMUM_POINTS("Point-Blocks.Minimum-Points", 4),

	MAXIMUM_POINTS("Point-Blocks.Maximum-Points", 8),

	WAIT_MILLISECONDS("Point-Blocks.Wait-Ms", 12);

	int defaultValue;

	ArenaOption(String path, int defaultValue) {
		Main plugin = JavaPlugin.getPlugin(Main.class);

		this.defaultValue = plugin.getConfig().getInt(path, defaultValue);
	}

	public int getDefaultValue() {
		return defaultValue;
	}
}