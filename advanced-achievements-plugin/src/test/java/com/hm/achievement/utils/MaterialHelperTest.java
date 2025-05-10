package com.hm.achievement.utils;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MaterialHelperTest {

    @Mock
    private PotionMeta potionMeta;

    @Mock
    private ItemStack itemStack;

    @Test
    void shouldReturnFalseForWaterPotion() {
        when(potionMeta.getBasePotionType()).thenReturn(PotionType.WATER);
        when(itemStack.getItemMeta()).thenReturn(potionMeta);
        when(itemStack.getType()).thenReturn(Material.POTION);
        MaterialHelper underTest = new MaterialHelper(null);

        assertFalse(underTest.isAnyPotionButWater(itemStack));
    }

    @Test
    void shouldReturnTrueForOtherPotion() {
        when(potionMeta.getBasePotionType()).thenReturn(PotionType.HARMING);
        when(itemStack.getItemMeta()).thenReturn(potionMeta);
        when(itemStack.getType()).thenReturn(Material.POTION);
        MaterialHelper underTest = new MaterialHelper(null);

        assertTrue(underTest.isAnyPotionButWater(itemStack));
    }

    @Test
    void shouldReturnFalseForOtherMaterial() {
        when(itemStack.getType()).thenReturn(Material.SPLASH_POTION);
        MaterialHelper underTest = new MaterialHelper(null);

        assertFalse(underTest.isAnyPotionButWater(itemStack));
    }
}
