package dev.despical.whackme.user;

import dev.despical.commons.reflection.XReflection;
import dev.despical.whackme.WhackMe;
import dev.despical.whackme.api.event.player.WMPlayerStatisticChangeEvent;
import dev.despical.whackme.stat.LocalStatistic;
import dev.despical.whackme.stat.StatisticType;
import dev.despical.whackme.arena.Arena;
import lombok.Setter;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * @author Despical
 * <p>
 * Created at 20.06.2022
 */
public class User {

    private static final WhackMe plugin = WhackMe.getInstance();
    private static long cooldownCounter;

    static {
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> cooldownCounter++, 20, 20);
    }

    private final UUID uuid;
    private final String playerName;
    private final Map<String, Double> cooldowns;
    private final Map<StatisticType, Integer> stats;

    @Setter
    private boolean editingMode;
    private double attackCooldown;

    public User(Player player) {
        this.uuid = player.getUniqueId();
        this.playerName = player.getName();
        this.cooldowns = new HashMap<>();
        this.stats = new HashMap<>();
    }

    public Arena getArena() {
        return plugin.getArenaRegistry().getArena(getPlayer());
    }

    public Player getPlayer() {
        return plugin.getServer().getPlayer(uuid);
    }

    public UUID getUniqueId() {
        return uuid;
    }

    public String getName() {
        return playerName;
    }

    public boolean isInEditingMode() {
        return editingMode;
    }

    public void sendRawMessage(final String message) {
        getPlayer().sendMessage(plugin.getChatManager().coloredRawMessage(message));
    }

    public int getStat(StatisticType statisticType) {
        return stats.computeIfAbsent(statisticType, stat -> 0);
    }

    public void setStat(StatisticType stat, int value) {
        stats.put(stat, value);

        plugin.callEvent(() -> new WMPlayerStatisticChangeEvent(getArena(), getPlayer(), stat, value));
    }

    public void addStat(StatisticType stat, int value) {
        setStat(stat, getStat(stat) + value);
    }

    public void resetStats() {
        for (var stat : LocalStatistic.values()) {
            stats.put(stat, 0);
        }
    }

    public void updateAttackCooldown() {
        if (!XReflection.supports(9)) return;

        Player player = this.getPlayer();

        if (player == null) return;

        Optional.ofNullable(player.getAttribute(Attribute.GENERIC_ATTACK_SPEED)).ifPresent(attribute -> {
            this.attackCooldown = attribute.getBaseValue();

            attribute.setBaseValue(plugin.getConfig().getDouble("Hit-Cooldown-Delay", 20));
        });
    }

    public void resetAttackCooldown() {
        if (!XReflection.supports(9)) return;

        Player player = this.getPlayer();

        if (player == null) return;

        Optional.ofNullable(player.getAttribute(Attribute.GENERIC_ATTACK_SPEED)).ifPresent(attribute -> {
            if (attackCooldown == 0) {
                attackCooldown = attribute.getDefaultValue();
            }

            attribute.setBaseValue(attackCooldown);
        });
    }

    public void setCooldown(String cooldown, double seconds) {
        cooldowns.put(cooldown, seconds + cooldownCounter);
    }

    public double getCooldown(String cooldown) {
        final Double remainingTime = cooldowns.get(cooldown);

        return (remainingTime == null || remainingTime <= cooldownCounter) ? 0 : remainingTime - cooldownCounter;
    }
}
