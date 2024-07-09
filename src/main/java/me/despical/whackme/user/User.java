package me.despical.whackme.user;

import me.despical.commons.reflection.XReflection;
import me.despical.whackme.WhackMe;
import me.despical.whackme.api.StatsStorage;
import me.despical.whackme.api.event.player.WMPlayerStatisticChangeEvent;
import me.despical.whackme.arena.Arena;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.stream.Stream;

/**
 * @author Despical
 * <p>
 * Created at 20.06.2022
 */
public class User {

	private static final WhackMe plugin = JavaPlugin.getPlugin(WhackMe.class);
	private static long cooldownCounter = 0;

	private final UUID uuid;
	private final String playerName;
	private final Map<String, Double> cooldowns;
	private final Map<StatsStorage.StatisticType, Integer> stats;

	private boolean editingMode;
	private double attackCooldown;

	public User(Player player) {
		this.uuid = player.getUniqueId();
		this.playerName = player.getName();
		this.cooldowns = new HashMap<>();
		this.stats = new EnumMap<>(StatsStorage.StatisticType.class);
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

	public void setEditingMode(boolean editingMode) {
		this.editingMode = editingMode;
	}

	public void sendRawMessage(final String message) {
		getPlayer().sendMessage(plugin.getChatManager().coloredRawMessage(message));
	}

	public int getStat(StatsStorage.StatisticType statisticType) {
		return stats.computeIfAbsent(statisticType, stat -> 0);
	}

	public void setStat(StatsStorage.StatisticType stat, int value) {
		stats.put(stat, value);

		// When disable initialized you can no longer create a scheduler
		if (plugin.isEnabled())
			plugin.getServer().getScheduler().runTask(plugin, () -> plugin.getServer().getPluginManager().callEvent(new WMPlayerStatisticChangeEvent(getArena(), getPlayer(), stat, value)));
	}

	public void addStat(StatsStorage.StatisticType stat, int value) {
		setStat(stat, getStat(stat) + value);
	}

	public void resetStats() {
		Stream.of(StatsStorage.StatisticType.values()).filter(stat -> !stat.isPersistent()).forEach(stat -> this.setStat(stat, 0));
	}

	public void updateAttackCooldown() {
		if (!XReflection.supports(9)) return;

		Player player = this.getPlayer();

		if (player == null) return;

		Optional.ofNullable(player.getAttribute(Attribute.GENERIC_ATTACK_SPEED)).ifPresent(attribute -> {
			this.attackCooldown = attribute.getBaseValue();

			attribute.setBaseValue(plugin.getConfig().getDouble("Hit-Cooldown-Delay", 4));
		});
	}

	public void resetAttackCooldown() {
		if (!XReflection.supports(9)) return;

		Player player = this.getPlayer();

		if (player == null) return;

		Optional.ofNullable(player.getAttribute(Attribute.GENERIC_ATTACK_SPEED)).ifPresent(attribute -> attribute.setBaseValue(this.attackCooldown));
	}

	public void setCooldown(String cooldown, double seconds) {
		cooldowns.put(cooldown, seconds + cooldownCounter);
	}

	public double getCooldown(String cooldown) {
		final Double remainingTime = cooldowns.get(cooldown);

		return (remainingTime == null || remainingTime <= cooldownCounter) ? 0 : remainingTime - cooldownCounter;
	}

	public static void cooldownHandlerTask() {
		plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> cooldownCounter++, 20, 20);
	}
}