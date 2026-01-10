package com.hm.achievement.listener.statistics;

import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.config.AchievementMap;
import com.hm.achievement.db.CacheManager;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerFishEvent;
import org.jspecify.annotations.NonNull;

/**
 * Listener class to deal with Fish achievements.
 *
 * @author Pyves
 */
@Singleton
public class FishListener extends AbstractListener {

    private Set<String> fishableFish;

    @Inject
    public FishListener(@Named("main") YamlConfiguration mainConfig, AchievementMap achievementMap,
                        CacheManager cacheManager) {
        super(NormalAchievements.FISH, mainConfig, achievementMap, cacheManager);
    }

    @Override
    public void extractConfigurationParameters() {
        super.extractConfigurationParameters();

        fishableFish = new HashSet<>();
        for (String block : mainConfig.getStringList("FishableFish")) {
            fishableFish.add(block.toUpperCase());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerFish(@NonNull PlayerFishEvent event) {
        if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH
                || !fishableFish.contains(((Item) Objects.requireNonNull(event.getCaught())).getItemStack().getType().name())) {
            return;
        }

        updateStatisticAndAwardAchievementsIfAvailable(event.getPlayer(), 1);
    }
}
