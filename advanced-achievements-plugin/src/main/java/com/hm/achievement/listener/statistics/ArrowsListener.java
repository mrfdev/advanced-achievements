package com.hm.achievement.listener.statistics;

import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.config.AchievementMap;
import com.hm.achievement.db.CacheManager;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityShootBowEvent;

/**
 * Listener class to deal with Arrows achievements.
 *
 * @author Pyves
 */
@Singleton
public class ArrowsListener extends AbstractListener {

    @Inject
    public ArrowsListener(@Named("main") YamlConfiguration mainConfig, AchievementMap achievementMap,
                          CacheManager cacheManager) {
        super(NormalAchievements.ARROWS, mainConfig, achievementMap, cacheManager);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityShootBow(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        updateStatisticAndAwardAchievementsIfAvailable((Player) event.getEntity(), 1);
    }
}
