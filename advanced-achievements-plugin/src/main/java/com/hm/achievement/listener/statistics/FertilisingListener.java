package com.hm.achievement.listener.statistics;

import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.config.AchievementMap;
import com.hm.achievement.db.CacheManager;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockFertilizeEvent;

/**
 * Listener class to deal with Fertilising achievements.
 *
 * @author Pyves
 */
@Singleton
public class FertilisingListener extends AbstractListener {

    @Inject
    public FertilisingListener(@Named("main") YamlConfiguration mainConfig, AchievementMap achievementMap,
                               CacheManager cacheManager) {
        super(NormalAchievements.FERTILISING, mainConfig, achievementMap, cacheManager);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockFertilize(BlockFertilizeEvent event) {
        if (event.getPlayer() != null) {
            updateStatisticAndAwardAchievementsIfAvailable(event.getPlayer(), 1);
        }
    }

}
