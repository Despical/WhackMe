package me.despical.whackme.user.data;

import me.despical.whackme.WhackMe;
import me.despical.whackme.api.statistics.StatisticType;
import me.despical.whackme.user.User;
import org.jetbrains.annotations.NotNull;

/**
 * @author Despical
 * <p>
 * Created at 20.06.2022
 */
public abstract class AbstractDatabase {

    @NotNull
    protected static final WhackMe plugin = WhackMe.getInstance();

    public abstract void saveStatistic(@NotNull User user, StatisticType statisticType);

    public abstract void saveStatistics(@NotNull User user);

    public abstract void saveAllStatistics();

    public abstract void loadStatistics(@NotNull User user);

    public abstract void shutdown();
}