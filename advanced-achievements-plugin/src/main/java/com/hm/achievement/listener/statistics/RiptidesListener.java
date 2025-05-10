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
import org.bukkit.event.player.PlayerRiptideEvent;

@Singleton
public class RiptidesListener extends AbstractListener {

    @Inject
    public RiptidesListener(@Named("main") YamlConfiguration mainConfig, AchievementMap achievementMap,
                            CacheManager cacheManager) {
        super(NormalAchievements.RIPTIDES, mainConfig, achievementMap, cacheManager);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerRiptide(PlayerRiptideEvent event) {
        updateStatisticAndAwardAchievementsIfAvailable(event.getPlayer(), 1);
    }
}
