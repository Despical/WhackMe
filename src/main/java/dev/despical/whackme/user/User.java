package dev.despical.whackme.user;

import dev.despical.whackme.WhackMe;
import dev.despical.whackme.arena.Arena;
import dev.despical.whackme.stats.StatisticType;
import dev.despical.whackme.stats.Statistics;
import dev.despical.whackme.util.Var;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * @author Despical
 * <p>
 * Created at 20.06.2022
 */
public class User {

    private static final WhackMe plugin;
    private static long cooldownCounter;

    static {
        plugin = WhackMe.getInstance();
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> cooldownCounter++, 20, 20);
    }

    @Getter
    @Setter
    private boolean inEditingMode;
    private final UUID uuid;

    @Getter
    private final String name;

    private final Map<String, Double> cooldowns;
    private final Map<StatisticType<?>, Object> stats;

    public User(Player player) {
        this.uuid = player.getUniqueId();
        this.name = player.getName();
        this.cooldowns = new HashMap<>();
        this.stats = new HashMap<>();
    }

    public UUID getUUID() {
        return uuid;
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(uuid);
    }

    public void ifPlayerPresent(Consumer<Player> playerConsumer) {
        Optional.ofNullable(getPlayer()).ifPresent(playerConsumer);
    }

    public void sendMessage(String path, Var... variables) {
        ifPlayerPresent(player -> plugin.getChatManager().sendMessage(player, path, variables));
    }

    public void sendRawMessage(String msg, Var... variables) {
        ifPlayerPresent(player -> {
            Component message = plugin.getChatManager().parseMessage(msg, variables);
            player.sendMessage(message);
        });
    }

    public void sendRawActionBarComponent(Component message) {
        ifPlayerPresent(player -> player.sendActionBar(message));
    }

    public void sendRawComponent(Component component, Var... vars) {
        ifPlayerPresent(player -> plugin.getChatManager().sendRawComponent(player, component, vars));
    }

    public void sendActionBar(String path, Var... variables) {
        ifPlayerPresent(player -> plugin.getChatManager().sendActionBar(player, path, variables));
    }

    public void sendRawActionBar(String msg, Var... variables) {
        ifPlayerPresent(player -> plugin.getChatManager().sendRawActionBar(player, msg, variables));
    }

    public Arena getArena() {
        return plugin.getArenaRegistry().getArena(this);
    }

    public boolean isInArena() {
        return getArena() != null;
    }

    public boolean hasCooldown(String cooldownName) {
        return getCooldown(cooldownName) > 0D;
    }

    public void setCooldown(String cooldownName, double seconds) {
        cooldowns.put(cooldownName, seconds + cooldownCounter);
    }

    public void removeCooldown(String cooldownName) {
        cooldowns.remove(cooldownName);
    }

    public double getCooldown(String cooldownName) {
        return Math.max(0D, cooldowns.getOrDefault(cooldownName, 0D) - cooldownCounter);
    }

    @SuppressWarnings("unchecked")
    public <T> T getStatistic(StatisticType<T> type) {
        return (T) stats.computeIfAbsent(type, stat -> {
            if (stat.getDefaultValue() instanceof Map) {
                return new HashMap<>();
            }

            return stat.getDefaultValue();
        });
    }

    public <T> void setStatistic(StatisticType<T> type, T newValue) {
        setStatisticInternal(type, newValue, true);
    }

    public <T> void loadStatistic(StatisticType<T> type, T newValue) {
        setStatisticInternal(type, newValue, false);
    }

    private <T> void setStatisticInternal(StatisticType<T> type, T newValue, boolean callEvent) {
        T oldValue = getStatistic(type);
        if (oldValue != null && oldValue.equals(newValue)) {
            return;
        }

        T finalValue = newValue;

        if (callEvent) {
            var event = plugin.getEventManager().statChange(getPlayer(), type, oldValue, newValue);

            if (event.isCancelled()) {
                return;
            }

            finalValue = event.getNewValue();
        }

        if (oldValue != null && oldValue.equals(finalValue)) {
            return;
        }

        stats.put(type, finalValue);
    }

    public void addStat(StatisticType<Integer> type, int amount) {
        setStatistic(type, getStatistic(type) + amount);
    }

    @SafeVarargs
    public final void addStat(StatisticType<Integer> type, StatisticType<Integer>... types) {
        addStat(type, 1);

        for (StatisticType<Integer> statisticType : types) {
            addStat(statisticType, 1);
        }
    }

    public void setStatisticIfHigher(StatisticType<Integer> type, int amount) {
        setStatistic(type, Math.max(getStatistic(type), amount));
    }

    public void resetTemporaryStats() {
        for (StatisticType<?> stat : Statistics.getTemporaryStats()) {
            stats.put(stat, stat.getDefaultValue());
        }
    }
}
