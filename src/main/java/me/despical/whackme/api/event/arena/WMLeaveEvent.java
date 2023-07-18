package me.despical.whackme.api.event.arena;

import me.despical.whackme.api.event.WMEvent;
import me.despical.whackme.arena.Arena;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class WMLeaveEvent extends WMEvent {

	private static final HandlerList HANDLERS = new HandlerList();

	private final Player player;

	public WMLeaveEvent(Player player, Arena arena) {
		super(arena);
		this.player = player;
	}

	public Player getPlayer() {
		return player;
	}

	public static HandlerList getHandlerList() {
		return HANDLERS;
	}

	public @NotNull HandlerList getHandlers() {
		return HANDLERS;
	}
}