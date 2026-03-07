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
import org.bukkit.event.player.PlayerDropItemEvent;

/**
 * Listener class to deal with ItemDrops achievements.
 *
 * @author Pyves
 */
@Singleton
public class DropsListener extends AbstractListener {

    @Inject
    public DropsListener(@Named("main") YamlConfiguration mainConfig, AchievementMap achievementMap,
                         CacheManager cacheManager) {
        super(NormalAchievements.DROPS, mainConfig, achievementMap, cacheManager);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        updateStatisticAndAwardAchievementsIfAvailable(event.getPlayer(), 1);
    }
}
