package me.despical.whackme.api.event.player;

import me.despical.whackme.api.event.WMEvent;
import me.despical.whackme.api.statistics.StatisticType;
import me.despical.whackme.arena.Arena;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * @author Despical
 * <p>
 * Created at 20.06.2022
 */
public class WMPlayerStatisticChangeEvent extends WMEvent {

    private static final HandlerList handlers = new HandlerList();

    private final Player player;
    private final StatisticType statisticType;
    private final int value;

    public WMPlayerStatisticChangeEvent(Arena arena, Player player, StatisticType statisticType, int value) {
        super(arena);
        this.player = player;
        this.statisticType = statisticType;
        this.value = value;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public Player getPlayer() {
        return player;
    }

    public StatisticType getStatisticType() {
        return statisticType;
    }

    public int getValue() {
        return value;
    }
}
