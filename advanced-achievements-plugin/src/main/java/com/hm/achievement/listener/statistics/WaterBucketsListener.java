package com.hm.achievement.listener.statistics;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.config.AchievementMap;
import com.hm.achievement.db.CacheManager;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerBucketFillEvent;

/**
 * Listener class to deal with WaterBuckets achievements.
 *
 * @author Pyves
 */
@Singleton
public class WaterBucketsListener extends AbstractRateLimitedListener {

    @Inject
    public WaterBucketsListener(@Named("main") YamlConfiguration mainConfig, AchievementMap achievementMap,
                                CacheManager cacheManager, AdvancedAchievements advancedAchievements,
                                @Named("lang") YamlConfiguration langConfig) {
        super(NormalAchievements.WATERBUCKETS, mainConfig, achievementMap, cacheManager, advancedAchievements, langConfig);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerBucketFill(PlayerBucketFillEvent event) {
        if (event.getItemStack().getType() == Material.WATER_BUCKET) {
            updateStatisticAndAwardAchievementsIfAvailable(event.getPlayer(), 1);
        }
    }
}
