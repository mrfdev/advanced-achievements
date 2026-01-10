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
import org.bukkit.event.enchantment.EnchantItemEvent;

/**
 * Listener class to deal with Enchantments achievements.
 *
 * @author Pyves
 */
@Singleton
public class EnchantmentsListener extends AbstractListener {

    @Inject
    public EnchantmentsListener(@Named("main") YamlConfiguration mainConfig, AchievementMap achievementMap,
                                CacheManager cacheManager) {
        super(NormalAchievements.ENCHANTMENTS, mainConfig, achievementMap, cacheManager);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEnchantItem(EnchantItemEvent event) {
        updateStatisticAndAwardAchievementsIfAvailable(event.getEnchanter(), 1);
    }
}
