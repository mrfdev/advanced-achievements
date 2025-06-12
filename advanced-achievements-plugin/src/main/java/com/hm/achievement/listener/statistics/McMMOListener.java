package com.hm.achievement.listener.statistics;

import com.gmail.nossr50.events.skills.McMMOPlayerSkillEvent;
import com.hm.achievement.category.MultipleAchievements;
import com.hm.achievement.config.AchievementMap;
import com.hm.achievement.db.CacheManager;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

@Singleton
public class McMMOListener extends AbstractListener implements Listener {

    private final Logger LOGGER = Logger.getLogger(McMMOListener.class.getName());

    @Inject
    public McMMOListener(@Named("main") YamlConfiguration yamlConfiguration, AchievementMap achievementMap, CacheManager cacheManager) {
        super(MultipleAchievements.MCMMO, yamlConfiguration, achievementMap, cacheManager);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSkillLevelUp(@NotNull McMMOPlayerSkillEvent event) {
        Player player = event.getPlayer();
        String skillName = event.getSkill().name().toLowerCase(Locale.ENGLISH);
        if (!player.hasPermission(category.toChildPermName(skillName))) {
            LOGGER.info("Player " + player.getName() + " missing permission for skill " + skillName);
            return;
        }
        Set<String> subcategories = new HashSet<>();
        addMatchingSubcategories(subcategories, skillName);
        subcategories.forEach(key -> {
            int prevSkillLevel = (int) cacheManager.getAndIncrementStatisticAmount(MultipleAchievements.MCMMO, key, player.getUniqueId(), 0);
            int levelDiff = event.getSkillLevel() - prevSkillLevel;
            LOGGER.info("Skill: " + skillName + ", key: " + key + ", prevSkillLevel from cache: " + prevSkillLevel + ", event level: " + event.getSkillLevel());
            if (levelDiff > 0) {
                long newLevel = cacheManager.getAndIncrementStatisticAmount(MultipleAchievements.MCMMO, key, player.getUniqueId(), levelDiff);
                LOGGER.info("Updated skill level in cache to: " + newLevel);
                updateStatisticAndAwardAchievementsIfAvailable(player, Collections.singleton(key), levelDiff);
            }
        });
    }
}