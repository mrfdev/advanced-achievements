package com.hm.achievement.utils;

import java.util.HashMap;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jspecify.annotations.NonNull;

public class InventoryHelper {

    private InventoryHelper() {
        // Not called.
    }

    /**
     * Calculates the space available to accommodate a new item stack. This method takes both empty slots and existing
     * similar item stacks into account.
     *
     * @param player
     * @param newItemStack
     * @return the available space for the item
     */
    public static int getAvailableSpace(@NonNull Player player, @NonNull ItemStack newItemStack) {
        int availableSpace = 0;
        PlayerInventory inventory = player.getInventory();
        HashMap<Integer, ? extends ItemStack> itemStacksWithSameMaterial = inventory.all(newItemStack.getType());
        for (ItemStack existingItemStack : itemStacksWithSameMaterial.values()) {
            if (newItemStack.isSimilar(existingItemStack)) availableSpace += (newItemStack.getMaxStackSize() - existingItemStack.getAmount());
        }
        for (ItemStack existingItemStack : inventory.getStorageContents()) {
            if (existingItemStack == null) availableSpace += newItemStack.getMaxStackSize();
        }
        return availableSpace;
    }
}
