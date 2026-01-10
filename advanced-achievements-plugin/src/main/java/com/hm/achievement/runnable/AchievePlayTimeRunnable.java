package com.hm.achievement.runnable;

import com.Zrips.CMI.CMI;
import com.earth2me.essentials.Essentials;
import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.config.AchievementMap;
import com.hm.achievement.db.CacheManager;
import com.hm.achievement.utils.StatisticIncreaseHandler;
import java.util.concurrent.TimeUnit;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
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

    private Essentials essentials;
    private long previousTimeMillis;

    private boolean configIgnoreAFKPlayedTime;

    @Inject
    public AchievePlayTimeRunnable(@Named("main") final YamlConfiguration mainConfig, final AchievementMap achievementMap,
                                   final CacheManager cacheManager) {
        super(mainConfig, achievementMap, cacheManager);

        if (Bukkit.getPluginManager().isPluginEnabled("Essentials"))
            this.essentials = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");

        this.previousTimeMillis = System.currentTimeMillis();
    }

    @Override
    public void extractConfigurationParameters() {
        super.extractConfigurationParameters();

        this.configIgnoreAFKPlayedTime = (this.essentials != null || Bukkit.getPluginManager().isPluginEnabled("CMI")) && this.mainConfig.getBoolean("IgnoreAFKPlayedTime");
    }

    @Override
    public void run() {
        final long currentTimeMillis = System.currentTimeMillis();
        final int millisSincePreviousRun = (int) (currentTimeMillis - this.previousTimeMillis);
        Bukkit.getOnlinePlayers().forEach(p -> this.updateTime(p, millisSincePreviousRun));
        this.previousTimeMillis = currentTimeMillis;
    }

    /**
     * Updates play time if all conditions are met and awards achievements if necessary.
     *
     * @param player
     * @param millisSincePreviousRun
     */
    private void updateTime(final Player player, final int millisSincePreviousRun) {
        if (!this.shouldIncreaseBeTakenIntoAccount(player, NormalAchievements.PLAYEDTIME))
            return;

        // If player is AFK, don't update played time.
        if (this.configIgnoreAFKPlayedTime && (this.essentials.getUser(player).isAfk() || CMI.getInstance().getPlayerManager().getUser(player).isAfk()))
            return;

        final long totalMillis = this.cacheManager.getAndIncrementStatisticAmount(NormalAchievements.PLAYEDTIME, player.getUniqueId(),
                millisSincePreviousRun);
        // Thresholds in the configuration are in hours.
        this.checkThresholdsAndAchievements(player, NormalAchievements.PLAYEDTIME, totalMillis / MILLIS_PER_HOUR);
    }
}
