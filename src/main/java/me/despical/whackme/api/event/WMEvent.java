package me.despical.whackme.api.event;

import me.despical.whackme.arena.Arena;
import org.bukkit.event.Event;

/**
 * @author Despical
 * <p>
 * Created at 20.06.2022
 */
public abstract class WMEvent extends Event {

	protected final Arena arena;

	public WMEvent(Arena arena) {
		this.arena = arena;
	}

	public Arena getArena() {
		return arena;
	}
}