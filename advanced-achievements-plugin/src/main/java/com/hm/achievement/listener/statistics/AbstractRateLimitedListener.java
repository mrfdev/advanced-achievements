package com.hm.achievement.listener.statistics;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.category.Category;
import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.config.AchievementMap;
import com.hm.achievement.db.CacheManager;
import com.hm.achievement.domain.Achievement;
import com.hm.achievement.lifecycle.Cleanable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Abstract class in charge of factoring out common functionality for the
 * listener classes with cooldown maps.
 *
 * @author Pyves
 */
public class AbstractRateLimitedListener extends AbstractListener implements Cleanable {

    private final Map<Integer, Map<UUID, Long>> slotsToPlayersLastActionTimes = new HashMap<>();
    private final AdvancedAchievements advancedAchievements;
    private final YamlConfiguration langConfig;

    private int categoryCooldown;
    private long hardestCategoryThreshold;
    private boolean configCooldownActionBar;

    private String langStatisticCooldown;

    AbstractRateLimitedListener(Category category, YamlConfiguration mainConfig, AchievementMap achievementMap,
                                CacheManager cacheManager, AdvancedAchievements advancedAchievements, YamlConfiguration langConfig) {
        super(category, mainConfig, achievementMap, cacheManager);
        this.advancedAchievements = advancedAchievements;
        this.langConfig = langConfig;
    }

    @Override
    public void extractConfigurationParameters() {
        super.extractConfigurationParameters();

        List<Achievement> achievements = achievementMap.getForCategory(category);
        hardestCategoryThreshold = achievements.isEmpty() ? Long.MAX_VALUE
                : achievements.getLast().getThreshold();
        categoryCooldown = mainConfig.getInt("StatisticCooldown." + category) * 1000;
        configCooldownActionBar = mainConfig.getBoolean("CooldownActionBar");
        langStatisticCooldown = langConfig.getString("statistic-cooldown");
    }

    @Override
    public void cleanPlayerData() {
        long currentTime = System.currentTimeMillis();
        slotsToPlayersLastActionTimes.values().forEach(playersLastActionTimes -> playersLastActionTimes.values()
                .removeIf(lastActionTime -> currentTime > lastActionTime + categoryCooldown));
    }

    void updateStatisticAndAwardAchievementsIfAvailable(Player player, int incrementValue, int slotNumber) {
        if (isInCooldownPeriod(player, slotNumber)) {
            super.updateStatisticAndAwardAchievementsIfAvailable(player, incrementValue);
        }
    }

    @Override
    void updateStatisticAndAwardAchievementsIfAvailable(Player player, int incrementValue) {
        if (isInCooldownPeriod(player, 0)) {
            super.updateStatisticAndAwardAchievementsIfAvailable(player, incrementValue);
        }
    }

    /**
     * Determines whether the player is in cooldown, i.e. a similar action was taken
     * into account too recently.
     *
     * @param player
     * @param slotNumber
     * @return true if the player is still in cooldown, false otherwise
     */
    private boolean isInCooldownPeriod(@NotNull Player player, int slotNumber) {
        UUID uuid = player.getUniqueId();
        long currentPlayerStatistic = cacheManager.getAndIncrementStatisticAmount((NormalAchievements) category, uuid,
                0);
        // Ignore cooldown if player has received all achievements in the category.
        if (currentPlayerStatistic >= hardestCategoryThreshold) {
            return true;
        }

        Map<UUID, Long> playersLastActionTimes = slotsToPlayersLastActionTimes.computeIfAbsent(slotNumber,
                HashMap::new);
        long currentTimeMillis = System.currentTimeMillis();
        long timeToWait = playersLastActionTimes.getOrDefault(uuid, 0L) + categoryCooldown - currentTimeMillis;
        if (timeToWait > 0) {
            if (configCooldownActionBar) {
                if (category == NormalAchievements.MUSICDISCS) {
                    // Display message with a delay to avoid it being overwritten by disc name
                    // message.
                    Bukkit.getScheduler().scheduleSyncDelayedTask(advancedAchievements,
                            () -> displayActionBarMessage(player, timeToWait), 20);
                } else {
                    displayActionBarMessage(player, timeToWait);
                }
            }
            return false;
        }
        playersLastActionTimes.put(uuid, currentTimeMillis);
        return true;
    }

    /**
     * Displays the cooldown action bar message.
     *
     * @param player
     * @param timeToWait
     */
    private void displayActionBarMessage(@NotNull Player player, long timeToWait) {
        String timeWithOneDecimal = String.format("%.1f", (double) timeToWait / 1000);
        String message = StringUtils.replaceEach(langStatisticCooldown, new String[] { "TIME" }, new String[] { timeWithOneDecimal });
        try (BukkitAudiences audiences = BukkitAudiences.create(advancedAchievements)) {
            audiences.player(player).sendActionBar(Component.text(message).decorate(TextDecoration.ITALIC));
        }
    }
}
