package com.hm.achievement.listener.statistics;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.config.AchievementMap;
import com.hm.achievement.db.CacheManager;
import com.hm.achievement.utils.InventoryHelper;
import com.hm.achievement.utils.MaterialHelper;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

/**
 * Listener class to deal with Brewing achievements.
 *
 * @author Pyves
 */
@Singleton
public class BrewingListener extends AbstractRateLimitedListener {

    private final MaterialHelper materialHelper;

    @Inject
    public BrewingListener(@Named("main") YamlConfiguration mainConfig, AchievementMap achievementMap,
                           CacheManager cacheManager, AdvancedAchievements advancedAchievements,
                           @Named("lang") YamlConfiguration langConfig, MaterialHelper materialHelper) {
        super(NormalAchievements.BREWING, mainConfig, achievementMap, cacheManager, advancedAchievements, langConfig);
        this.materialHelper = materialHelper;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        ItemStack item = event.getCurrentItem();
        if (event.getInventory().getType() != InventoryType.BREWING || event.getAction() == InventoryAction.NOTHING
                || event.getClick() == ClickType.NUMBER_KEY && event.getAction() == InventoryAction.HOTBAR_SWAP
                || !isBrewablePotion(item)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        int eventAmount = item.getAmount();
        if (event.isShiftClick()) {
            eventAmount = Math.min(eventAmount, InventoryHelper.getAvailableSpace(player, item));
            if (eventAmount == 0) {
                return;
            }
        }

        updateStatisticAndAwardAchievementsIfAvailable(player, eventAmount, event.getRawSlot());
    }

    /**
     * Determine whether the event corresponds to a brewable potion, i.e. not water.
     *
     * @param item
     * @return true if for any brewable potion
     */
    private boolean isBrewablePotion(ItemStack item) {
        return item != null && (materialHelper.isAnyPotionButWater(item) || item.getType() == Material.SPLASH_POTION);
    }
}
