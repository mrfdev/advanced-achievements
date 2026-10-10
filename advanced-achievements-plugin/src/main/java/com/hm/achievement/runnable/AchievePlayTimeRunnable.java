package com.hm.achievement.runnable;

import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.config.AchievementMap;
import com.hm.achievement.db.CacheManager;
import com.hm.achievement.utils.AfkTracker;
import com.hm.achievement.utils.StatisticIncreaseHandler;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import java.util.concurrent.TimeUnit;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

/**
 * Class used to monitor players' played times.
 *
 * @author Pyves
 */
@Singleton
public class AchievePlayTimeRunnable extends StatisticIncreaseHandler implements Runnable {

    private static final long MILLIS_PER_HOUR = TimeUnit.HOURS.toMillis(1);
    private final AfkTracker afkTracker;
    private long previousTime;
    private boolean configIgnoreAfkPlayedTime;

    @Inject
    public AchievePlayTimeRunnable(@Named("main") final YamlConfiguration mainConfig, final AchievementMap achievementMap, final CacheManager cacheManager, final AfkTracker afkTracker) {
        super(mainConfig, achievementMap, cacheManager);
        this.afkTracker = afkTracker;
        this.previousTime = System.currentTimeMillis();
    }

    @Override
    public void extractConfigurationParameters() {
        super.extractConfigurationParameters();
        this.configIgnoreAfkPlayedTime = this.mainConfig.getBoolean("IgnoreAFKPlayedTime", false);
    }

    @Override
    public void run() {
        final long current = System.currentTimeMillis();
        final int millisSincePrevious = (int) (current - this.previousTime);
        Bukkit.getOnlinePlayers().forEach(p -> this.updateTime(p, millisSincePrevious));
        this.previousTime = current;
    }

    private void updateTime(final Player player, final int millisSincePrevious) {
        if (!this.shouldIncreaseBeTakenIntoAccount(player, NormalAchievements.PLAYEDTIME)) return;
        if (this.configIgnoreAfkPlayedTime && this.afkTracker.isAfk(player)) return;
        final long total = this.cacheManager.getAndIncrementStatisticAmount(NormalAchievements.PLAYEDTIME, player.getUniqueId(), millisSincePrevious);
        this.checkThresholdsAndAchievements(player, NormalAchievements.PLAYEDTIME, total / MILLIS_PER_HOUR);
    }
}
