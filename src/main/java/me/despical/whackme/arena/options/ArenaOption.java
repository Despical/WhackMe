package me.despical.whackme.arena.options;

import me.despical.whackme.WhackMe;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.function.Function;

/**
 * @author Despical
 * <p>
 * Created at 20.06.2022
 */
public enum ArenaOption {

	TIMER(config -> config.getInt("Gameplay-Time", 30)),

	MINIMUM_POINTS(4),

	MAXIMUM_POINTS(8),

	WAIT_MILLISECONDS(config -> config.getInt("Point-Blocks.Wait-Ms", 12));

	private final Object value;

	ArenaOption(int defaultValue) {
		this.value = defaultValue;
	}

	ArenaOption(Function<FileConfiguration, Object> function) {
		this.value = function.apply(WhackMe.getInstance().getConfig());
	}

	@SuppressWarnings("unchecked")
	public <T> T getDefault() {
		return (T) this.value;
	}
}