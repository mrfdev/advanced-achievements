package com.hm.achievement.listener;

import com.hm.achievement.category.Category;
import com.hm.achievement.category.MultipleAchievements;
import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.gui.AchievementInventoryHolder;
import com.hm.achievement.gui.CategoryGUI;
import com.hm.achievement.gui.GUIItems;
import com.hm.achievement.gui.MainGUI;
import java.util.Set;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jspecify.annotations.NonNull;

import static com.hm.achievement.gui.AchievementInventoryHolder.MAIN_GUI_PAGE;
import static com.hm.achievement.gui.CategoryGUI.ROW_SIZE;

/**
 * Listener class to deal with the GUIs from the /aach list command.
 *
 * @author Pyves
 */
public class ListGUIListener implements Listener {

    private final YamlConfiguration mainConfig;
    private final Set<Category> disabledCategories;
    private final MainGUI mainGUI;
    private final CategoryGUI categoryGUI;
    private final GUIItems guiItems;

    @Inject
    public ListGUIListener(@Named("main") YamlConfiguration mainConfig, Set<Category> disabledCategories, MainGUI mainGUI, CategoryGUI categoryGUI, GUIItems guiItems) {
        this.mainConfig = mainConfig;
        this.disabledCategories = disabledCategories;
        this.mainGUI = mainGUI;
        this.categoryGUI = categoryGUI;
        this.guiItems = guiItems;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryClick(@NonNull InventoryClickEvent event) {
        Inventory inventory = event.getInventory();
        if (!(inventory.getHolder() instanceof AchievementInventoryHolder holder) || event.getRawSlot() < 0) {
            return;
        }

        // Prevent players from taking items out of the GUI.
        event.setCancelled(true);

        ItemStack clickedItem = event.getCurrentItem();
        Player player = (Player) event.getWhoClicked();
        if (clickedItem == null || !clickedItem.hasItemMeta()) {
            return;
        }

        int currentPage = holder.getPageIndex();

        // Handle back button via PersistentDataContainer
        ItemMeta meta = clickedItem.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        if (pdc.has(CategoryGUI.BACK_BUTTON_KEY, PersistentDataType.BYTE)) {
            String command = mainConfig.getString("OverrideBackButtonBehaviour");
            if (StringUtils.isBlank(command)) {
                mainGUI.displayMainGUI(player);
            } else {
                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), Strings.CS.replace(command, "PLAYER", player.getName()));
            }
            return;
        }

        // Handle main GUI clicks
        if (currentPage == MAIN_GUI_PAGE) {
            if (!clickedItem.isSimilar(guiItems.getCategoryLock()) && event.getRawSlot() < getMainGUIItemCount()) {
                categoryGUI.displayCategoryGUI(clickedItem, player, 0);
            }
            return;
        }

        // Handle prev/next navigation in category GUIs
        if (event.getRawSlot() == inventory.getSize() - ROW_SIZE) {
            categoryGUI.displayCategoryGUI(holder.getCategoryItem(), player, currentPage - 1);
        } else if (event.getRawSlot() == inventory.getSize() - 1) {
            categoryGUI.displayCategoryGUI(holder.getCategoryItem(), player, currentPage + 1);
        }
    }

    /**
     * Returns the number of items in the main GUI.
     *
     * @return the count of non disabled categories
     */
    private int getMainGUIItemCount() {
        return NormalAchievements.values().length + MultipleAchievements.values().length - disabledCategories.size() + 1;
    }
}