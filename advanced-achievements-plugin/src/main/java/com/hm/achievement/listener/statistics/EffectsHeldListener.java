package com.hm.achievement.listener.statistics;

import com.hm.achievement.category.MultipleAchievements;
import com.hm.achievement.config.AchievementMap;
import com.hm.achievement.db.CacheManager;
import java.util.HashSet;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.jspecify.annotations.NonNull;

@Singleton
public class EffectsHeldListener extends AbstractListener {

    @Inject
    public EffectsHeldListener(@Named("main") YamlConfiguration mainConfig, AchievementMap achievementMap, CacheManager cacheManager) {
        super(MultipleAchievements.EFFECTSHELD, mainConfig, achievementMap, cacheManager);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityPotionEffectEvent(@NonNull EntityPotionEffectEvent event) {
        if (!(event.getEntity() instanceof Player player) || event.getNewEffect() == null) {
            return;
        }

        String effectName = event.getNewEffect().getType().getKey().getKey().toLowerCase();
        if (!player.hasPermission(category.toChildPermName(effectName))) {
            return;
        }

        Set<String> subcategories = new HashSet<>();

        addMatchingSubcategories(subcategories, effectName);
        updateStatisticAndAwardAchievementsIfAvailable(player, subcategories, 1);
    }
}
