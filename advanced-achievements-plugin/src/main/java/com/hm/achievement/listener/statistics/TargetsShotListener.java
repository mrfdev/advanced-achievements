package com.hm.achievement.listener.statistics;

import com.hm.achievement.category.MultipleAchievements;
import com.hm.achievement.config.AchievementMap;
import com.hm.achievement.db.CacheManager;
import java.util.HashSet;
import java.util.Set;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.ProjectileHitEvent;

@Singleton
public class TargetsShotListener extends AbstractListener {

    @Inject
    public TargetsShotListener(@Named("main") YamlConfiguration mainConfig, AchievementMap achievementMap,
                               CacheManager cacheManager) {
        super(MultipleAchievements.TARGETSSHOT, mainConfig, achievementMap, cacheManager);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!(event.getEntity().getShooter() instanceof Player player)) {
            return;
        }

        String targetName;
        if (event.getHitEntity() != null) {
            targetName = event.getHitEntity().getType().name().toLowerCase();
        } else if (event.getHitBlock() != null) {
            targetName = event.getHitBlock().getType().name().toLowerCase();
        } else {
            return;
        }

        if (!player.hasPermission(category.toChildPermName(targetName))) {
            return;
        }

        Set<String> subcategories = new HashSet<>();

        addMatchingSubcategories(subcategories, targetName);
        updateStatisticAndAwardAchievementsIfAvailable(player, subcategories, 1);
    }
}
