package com.hm.achievement.listener.statistics;

import com.hm.achievement.category.MultipleAchievements;
import com.hm.achievement.config.AchievementMap;
import com.hm.achievement.db.CacheManager;
import com.hm.achievement.utils.InventoryHelper;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.jspecify.annotations.NonNull;

/**
 * Listener class to deal with Crafts achievements.
 *
 * @author Pyves
 */
@Singleton
public class CraftsListener extends AbstractListener {

    @Inject
    public CraftsListener(@Named("main") YamlConfiguration mainConfig, AchievementMap achievementMap,
                          CacheManager cacheManager) {
        super(MultipleAchievements.CRAFTS, mainConfig, achievementMap, cacheManager);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCraftItem(@NonNull CraftItemEvent event) {
        if (!(event.getWhoClicked() instanceof Player player) || event.getAction() == InventoryAction.NOTHING
                || event.getClick() == ClickType.NUMBER_KEY && event.getAction() == InventoryAction.HOTBAR_SWAP
                || isCraftingIngotFromBlock(event.getRecipe())) {
            return;
        }

        ItemStack item = event.getRecipe().getResult();
        String craftName = Objects.requireNonNull(item).getType().name().toLowerCase();
        if (!player.hasPermission(category.toChildPermName(craftName))) {
            return;
        }

        Set<String> subcategories = new HashSet<>();
        addMatchingSubcategories(subcategories, craftName);

        if (item.getType().name().endsWith("_BANNER")) {
            addMatchingSubcategories(subcategories, "banner");
        }

        int eventAmount = item.getAmount();
        if (event.isShiftClick()) {
            int maxAmount = event.getInventory().getMaxStackSize();
            ItemStack[] matrix = event.getInventory().getMatrix();
            for (ItemStack itemStack : matrix) {
                if (itemStack != null && itemStack.getType() != Material.AIR) {
                    int itemStackAmount = itemStack.getAmount();
                    if (itemStackAmount < maxAmount && itemStackAmount > 0) {
                        maxAmount = itemStackAmount;
                    }
                }
            }
            eventAmount *= maxAmount;
            eventAmount = Math.min(eventAmount, InventoryHelper.getAvailableSpace(player, item));
            if (eventAmount == 0) {
                return;
            }
        }

        updateStatisticAndAwardAchievementsIfAvailable(player, subcategories, eventAmount);
    }

    /**
     * Metal blocks can be used for repeated crafts of ingots (e.g. iron block -> 9 iron ingots -> iron block -> ...).
     * Detect and prevent this.
     *
     * @param recipe
     * @return true if the player is trying to craft ingots from a block of the same metal
     */
    private boolean isCraftingIngotFromBlock(Recipe recipe) {
        Material ingredient = Material.AIR;
        if (recipe instanceof ShapelessRecipe shapelessRecipe) {
            List<RecipeChoice> ingredientList = shapelessRecipe.getChoiceList();
            if (ingredientList.size() == 1) {
                RecipeChoice recipeChoice = ingredientList.getFirst();
                if (recipeChoice instanceof RecipeChoice.MaterialChoice materialChoice && materialChoice.getChoices().size() == 1) {
                    ingredient = materialChoice.getChoices().getFirst();
                }
            }
        } else if (recipe instanceof ShapedRecipe shapedRecipe) {
            Collection<RecipeChoice> choices = shapedRecipe.getChoiceMap().values();
            if (choices.size() == 1) {
                RecipeChoice recipeChoice = choices.iterator().next();
                if (recipeChoice instanceof RecipeChoice.MaterialChoice materialChoice && materialChoice.getChoices().size() == 1) {
                    ingredient = materialChoice.getChoices().getFirst();
                }
            }
        }
        return ingredient.name().endsWith("_BLOCK") && recipe.getResult().getType().name().endsWith("_INGOT");
    }
}