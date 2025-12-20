package dev.despical.whackme.user.data;

import dev.despical.whackme.WhackMe;
import dev.despical.whackme.api.statistics.StatisticType;
import dev.despical.whackme.user.User;
import org.jetbrains.annotations.NotNull;

/**
 * @author Despical
 * <p>
 * Created at 20.06.2022
 */
public abstract class UserDatabase {

    @NotNull
    protected static final WhackMe plugin = WhackMe.getInstance();

    public abstract void saveStatistic(@NotNull User user, StatisticType statisticType);

    public abstract void saveStatistics(@NotNull User user);

    public abstract void saveAllStatistics();

    public abstract void loadStatistics(@NotNull User user);

    public abstract void shutdown();
}
