package com.hm.achievement.listener.statistics;

import com.hm.achievement.category.MultipleAchievements;
import com.hm.achievement.config.AchievementMap;
import com.hm.achievement.db.CacheManager;
import java.util.HashSet;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jspecify.annotations.NonNull;

/**
 * Listener class to deal with Places achievements.
 *
 * @author Pyves
 */
@Singleton
public class PlacesListener extends AbstractListener {

    @Inject
    public PlacesListener(@Named("main") YamlConfiguration mainConfig, AchievementMap achievementMap,
                          CacheManager cacheManager) {
        super(MultipleAchievements.PLACES, mainConfig, achievementMap, cacheManager);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(@NonNull BlockPlaceEvent event) {
        Player player = event.getPlayer();
        ItemStack placedItem = event.getItemInHand();

        Set<String> subcategories = new HashSet<>();

        String blockName = placedItem.getType().name().toLowerCase();
        if (player.hasPermission(category.toChildPermName(blockName))) {
            addMatchingSubcategories(subcategories, blockName);
        }

        ItemMeta itemMeta = placedItem.getItemMeta();
        if (itemMeta != null && itemMeta.hasDisplayName()) {
            Component displayNameComp = itemMeta.customName();
            if (displayNameComp != null) {
                String displayName = LegacyComponentSerializer.legacySection().serialize(displayNameComp);
                if (player.hasPermission(category.toChildPermName(StringUtils.deleteWhitespace(displayName)))) {
                    addMatchingSubcategories(subcategories, displayName);
                }
            }
        }
        updateStatisticAndAwardAchievementsIfAvailable(player, subcategories, 1);
    }
}
