package me.despical.whackme.arena.options;

/**
 * @author Despical
 * <p>
 * Created at 20.06.2022
 */
public enum ArenaOption {

	TIMER(30),

	MINIMUM_POINTS(4),

	MAXIMUM_POINTS(8);

	int defaultValue;

	ArenaOption(int defaultValue) {
		this.defaultValue = defaultValue;
	}

	public int getDefaultValue() {
		return defaultValue;
	}
}