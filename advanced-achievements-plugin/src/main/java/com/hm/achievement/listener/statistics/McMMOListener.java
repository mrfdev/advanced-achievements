package com.hm.achievement.listener.statistics;

import com.gmail.nossr50.api.ExperienceAPI;
import com.gmail.nossr50.datatypes.skills.PrimarySkillType;
import com.gmail.nossr50.events.experience.McMMOPlayerLevelUpEvent;
import com.gmail.nossr50.events.party.McMMOPartyLevelUpEvent;
import com.gmail.nossr50.events.skills.abilities.McMMOPlayerAbilityActivateEvent;
import com.gmail.nossr50.util.player.UserManager;
import com.hm.achievement.category.MultipleAchievements;
import com.hm.achievement.config.AchievementMap;
import com.hm.achievement.db.CacheManager;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jspecify.annotations.NonNull;

@Singleton
public class McMMOListener extends AbstractListener implements Listener {

    private final Logger LOGGER = Logger.getLogger(McMMOListener.class.getName());

    @Inject
    public McMMOListener(@Named("main") YamlConfiguration yamlConfiguration, AchievementMap achievementMap, CacheManager cacheManager) {
        super(MultipleAchievements.MCMMO, yamlConfiguration, achievementMap, cacheManager);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSkillLevelUp(@NonNull McMMOPlayerLevelUpEvent event) {
        Player player = event.getPlayer();
        String skillName = event.getSkill().name().toLowerCase(Locale.ENGLISH);
        String permission = category.toChildPermName(skillName);
        if (!player.hasPermission(permission)) {
            LOGGER.info("Player " + player.getName() + " missing permission " + permission + " for skill " + skillName);
            return;
        }
        Set<String> subcategories = new HashSet<>();
        addMatchingSubcategories(subcategories, skillName);
        subcategories.forEach(key -> {
            int prevSkillLevel = (int) cacheManager.getAndIncrementStatisticAmount(MultipleAchievements.MCMMO, key, player.getUniqueId(), 0);
            int levelDiff = event.getSkillLevel() - prevSkillLevel;
            if (levelDiff > 0) {
                updateStatisticAndAwardAchievementsIfAvailable(player, Collections.singleton(key), levelDiff);
            }
        });
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPowerLevelUp(@NonNull McMMOPlayerLevelUpEvent event) {
        Player player = event.getPlayer();
        int powerLevel = ExperienceAPI.getPowerLevel(player);
        int oldPowerLevel = (int) cacheManager.getAndIncrementStatisticAmount(MultipleAchievements.MCMMO, "power", player.getUniqueId(), 0);
        int powerDiff = powerLevel - oldPowerLevel;
        if (powerDiff > 0) {
            updateStatisticAndAwardAchievementsIfAvailable(player, Collections.singleton("power"), powerDiff);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onAbilityActivation(@NonNull McMMOPlayerAbilityActivateEvent event) {
        Player player = event.getPlayer();
        cacheManager.getAndIncrementStatisticAmount(MultipleAchievements.MCMMO, "ability", player.getUniqueId(), 0);
        updateStatisticAndAwardAchievementsIfAvailable(player, Collections.singleton("ability"), 1);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPartyLevelUp(@NonNull McMMOPartyLevelUpEvent event) {
        int levelsGained = event.getLevelsChanged();
        if (levelsGained <= 0) return;
        event.getParty().getMembers().keySet().forEach(uuid -> {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) return;
            cacheManager.getAndIncrementStatisticAmount(MultipleAchievements.MCMMO, "party_level", uuid, levelsGained);
            updateStatisticAndAwardAchievementsIfAvailable(player, Collections.singleton("party_level"), levelsGained);
        });
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(@NonNull PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Bukkit.getScheduler().runTaskLater(Objects.requireNonNull(Bukkit.getPluginManager().getPlugin("AdvancedAchievements")), () -> {
            if (!player.isOnline()) return;
            if (!UserManager.hasPlayerDataKey(player)) {
                LOGGER.warning("Cannot synchronise mcMMO achievements for " + player.getName() + ": mcMMO player data is not loaded.");
                return;
            }
            for (PrimarySkillType skill : PrimarySkillType.values()) {
                String name = skill.name().toLowerCase(Locale.ENGLISH);
                String permission = category.toChildPermName(name);
                if (!player.hasPermission(permission)) continue;
                Set<String> subcategories = new HashSet<>();
                addMatchingSubcategories(subcategories, name);
                if (subcategories.isEmpty()) continue;
                int skillLevel = ExperienceAPI.getLevel(player, skill);
                for (String key : subcategories) {
                    int storedLevel = (int) cacheManager.getAndIncrementStatisticAmount(MultipleAchievements.MCMMO, key, player.getUniqueId(), 0);
                    int levelDiff = skillLevel - storedLevel;
                    if (levelDiff > 0) updateStatisticAndAwardAchievementsIfAvailable(player, Collections.singleton(key), levelDiff);
                }
            }
            int powerLevel = ExperienceAPI.getPowerLevel(player);
            int storedPowerLevel = (int) cacheManager.getAndIncrementStatisticAmount(MultipleAchievements.MCMMO, "power", player.getUniqueId(), 0);
            int powerDiff = powerLevel - storedPowerLevel;
            if (powerDiff > 0) updateStatisticAndAwardAchievementsIfAvailable(player, Collections.singleton("power"), powerDiff);
        }, 100L);
    }
}