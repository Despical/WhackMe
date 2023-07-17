package me.despical.whackme.api.event.arena;

import me.despical.whackme.api.event.WMEvent;
import me.despical.whackme.arena.Arena;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class WMJoinEvent extends WMEvent implements Cancellable {

	private static final HandlerList HANDLERS = new HandlerList();

	private final Player player;

	private boolean isCancelled;

	public WMJoinEvent(Player player, Arena arena) {
		super(arena);
		this.player = player;
		this.isCancelled = false;
	}

	public boolean isCancelled() {
		return this.isCancelled;
	}

	public void setCancelled(boolean isCancelled) {
		this.isCancelled = isCancelled;
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