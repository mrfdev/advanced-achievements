package com.hm.achievement.utils;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.boss.BarColor;

public class ColorHelper {

    private ColorHelper() {
        // Not called.
    }

    public static Color convertChatColorToColor(ChatColor chatColor) {
        return switch (chatColor) {
            case AQUA -> Color.fromRGB(0x55, 0xFF, 0xFF);
            case BLACK -> Color.BLACK;
            case BLUE -> Color.fromRGB(0x55, 0x55, 0xFF);
            case GRAY -> Color.fromRGB(0xAA, 0xAA, 0xAA);
            case DARK_AQUA -> Color.fromRGB(0x00, 0xAA, 0xAA);
            case DARK_BLUE -> Color.fromRGB(0x00, 0x00, 0xAA);
            case DARK_GRAY -> Color.fromRGB(0x55, 0x55, 0x55);
            case DARK_GREEN -> Color.fromRGB(0x00, 0xAA, 0x00);
            case DARK_PURPLE -> Color.fromRGB(0xAA, 0x00, 0xAA);
            case DARK_RED -> Color.fromRGB(0xAA, 0x00, 0x00);
            case GOLD -> Color.fromRGB(0xFF, 0xAA, 0x00);
            case GREEN -> Color.fromRGB(0x55, 0xFF, 0x55);
            case LIGHT_PURPLE -> Color.fromRGB(0xFF, 0x55, 0xFF);
            case RED -> Color.fromRGB(0xFF, 0x55, 0x55);
            case WHITE -> Color.WHITE;
            case YELLOW -> Color.fromRGB(0xFF, 0xFF, 0x55);
            default -> Color.WHITE;
        };
    }

    public static BarColor convertChatColorToBarColor(ChatColor chatColor) {
        return switch (chatColor) {
            case AQUA -> BarColor.GREEN;
            case BLACK -> BarColor.PURPLE;
            case BLUE -> BarColor.BLUE;
            case GRAY -> BarColor.WHITE;
            case DARK_AQUA -> BarColor.BLUE;
            case DARK_BLUE -> BarColor.BLUE;
            case DARK_GRAY -> BarColor.PURPLE;
            case DARK_GREEN -> BarColor.GREEN;
            case DARK_PURPLE -> BarColor.PURPLE;
            case DARK_RED -> BarColor.RED;
            case GOLD -> BarColor.YELLOW;
            case GREEN -> BarColor.GREEN;
            case LIGHT_PURPLE -> BarColor.PURPLE;
            case RED -> BarColor.RED;
            case WHITE -> BarColor.WHITE;
            case YELLOW -> BarColor.YELLOW;
            default -> BarColor.WHITE;
        };
    }

}