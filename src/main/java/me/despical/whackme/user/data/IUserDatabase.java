package me.despical.whackme.user.data;

import me.despical.whackme.WhackMe;
import me.despical.whackme.api.StatsStorage;
import me.despical.whackme.user.User;
import org.jetbrains.annotations.NotNull;

/**
 * @author Despical
 * <p>
 * Created at 20.06.2022
 */
public abstract class IUserDatabase {

	@NotNull
	protected final WhackMe plugin;

	public IUserDatabase(final @NotNull WhackMe plugin) {
		this.plugin = plugin;
	}

	public abstract void saveStatistic(final @NotNull User user, final StatsStorage.StatisticType statisticType);

	public abstract void saveStatistics(final @NotNull User user);

	public abstract void loadStatistics(final @NotNull User user);
}