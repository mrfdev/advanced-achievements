package com.hm.achievement.listener.statistics;

import com.gamingmesh.jobs.api.JobsLevelUpEvent;
import com.hm.achievement.category.MultipleAchievements;
import com.hm.achievement.config.AchievementMap;
import com.hm.achievement.db.CacheManager;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

/**
 * Listener class to deal with Jobs Reborn achievements.
 */
@Singleton
public class JobsRebornListener extends AbstractListener {

    @Inject
    public JobsRebornListener(@Named("main") YamlConfiguration mainConfig, AchievementMap achievementMap,
                              CacheManager cacheManager) {
        super(MultipleAchievements.JOBSREBORN, mainConfig, achievementMap, cacheManager);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onJob(JobsLevelUpEvent event) {
        // Get the Player from the JobsPlayer.
        Player player = event.getPlayer().getPlayer();
        if (player == null) {
            return;
        }

        String jobName = event.getJob().getJobFullName().toLowerCase();
        if (!player.hasPermission(category.toChildPermName(jobName))) {
            return;
        }

        Set<String> subcategories = new HashSet<>();
        addMatchingSubcategories(subcategories, jobName);
        subcategories.forEach(key -> {
            int previousJobLevel = (int) cacheManager.getAndIncrementStatisticAmount(MultipleAchievements.JOBSREBORN, key,
                    player.getUniqueId(), 0);
            int levelDiff = event.getLevel() - previousJobLevel;
            if (levelDiff > 0) {
                updateStatisticAndAwardAchievementsIfAvailable(player, Collections.singleton(key), levelDiff);
            }
        });
    }
}
