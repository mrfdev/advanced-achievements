package com.hm.achievement.listener.statistics;

import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.config.AchievementMap;
import com.hm.achievement.db.CacheManager;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PlayerDeathEvent;

/**
 * Listener class to deal with Deaths achievements.
 *
 * @author Pyves
 */
@Singleton
public class DeathsListener extends AbstractListener {

    @Inject
    public DeathsListener(@Named("main") YamlConfiguration mainConfig, AchievementMap achievementMap,
                          CacheManager cacheManager) {
        super(NormalAchievements.DEATHS, mainConfig, achievementMap, cacheManager);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        updateStatisticAndAwardAchievementsIfAvailable(event.getEntity(), 1);
    }
}
