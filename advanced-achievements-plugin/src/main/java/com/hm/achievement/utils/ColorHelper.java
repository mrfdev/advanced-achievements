package com.hm.achievement.utils;

import java.util.Map;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Color;
import org.bukkit.boss.BarColor;

public class ColorHelper {

    private static final Map<NamedTextColor, Color> COLOR_MAP = Map.ofEntries(
            Map.entry(NamedTextColor.AQUA, Color.fromRGB(0x55, 0xFF, 0xFF)),
            Map.entry(NamedTextColor.BLACK, Color.BLACK),
            Map.entry(NamedTextColor.BLUE, Color.fromRGB(0x55, 0x55, 0xFF)),
            Map.entry(NamedTextColor.GRAY, Color.fromRGB(0xAA, 0xAA, 0xAA)),
            Map.entry(NamedTextColor.DARK_AQUA, Color.fromRGB(0x00, 0xAA, 0xAA)),
            Map.entry(NamedTextColor.DARK_BLUE, Color.fromRGB(0x00, 0x00, 0xAA)),
            Map.entry(NamedTextColor.DARK_GRAY, Color.fromRGB(0x55, 0x55, 0x55)),
            Map.entry(NamedTextColor.DARK_GREEN, Color.fromRGB(0x00, 0xAA, 0x00)),
            Map.entry(NamedTextColor.DARK_PURPLE, Color.fromRGB(0xAA, 0x00, 0xAA)),
            Map.entry(NamedTextColor.DARK_RED, Color.fromRGB(0xAA, 0x00, 0x00)),
            Map.entry(NamedTextColor.GOLD, Color.fromRGB(0xFF, 0xAA, 0x00)),
            Map.entry(NamedTextColor.GREEN, Color.fromRGB(0x55, 0xFF, 0x55)),
            Map.entry(NamedTextColor.LIGHT_PURPLE, Color.fromRGB(0xFF, 0x55, 0xFF)),
            Map.entry(NamedTextColor.RED, Color.fromRGB(0xFF, 0x55, 0x55)),
            Map.entry(NamedTextColor.WHITE, Color.WHITE),
            Map.entry(NamedTextColor.YELLOW, Color.fromRGB(0xFF, 0xFF, 0x55))
    );

    private static final Map<NamedTextColor, BarColor> BAR_COLOR_MAP = Map.ofEntries(
            Map.entry(NamedTextColor.AQUA, BarColor.GREEN),
            Map.entry(NamedTextColor.BLACK, BarColor.PURPLE),
            Map.entry(NamedTextColor.BLUE, BarColor.BLUE),
            Map.entry(NamedTextColor.GRAY, BarColor.WHITE),
            Map.entry(NamedTextColor.DARK_AQUA, BarColor.BLUE),
            Map.entry(NamedTextColor.DARK_BLUE, BarColor.BLUE),
            Map.entry(NamedTextColor.DARK_GRAY, BarColor.PURPLE),
            Map.entry(NamedTextColor.DARK_GREEN, BarColor.GREEN),
            Map.entry(NamedTextColor.DARK_PURPLE, BarColor.PURPLE),
            Map.entry(NamedTextColor.DARK_RED, BarColor.RED),
            Map.entry(NamedTextColor.GOLD, BarColor.YELLOW),
            Map.entry(NamedTextColor.GREEN, BarColor.GREEN),
            Map.entry(NamedTextColor.LIGHT_PURPLE, BarColor.PURPLE),
            Map.entry(NamedTextColor.RED, BarColor.RED),
            Map.entry(NamedTextColor.WHITE, BarColor.WHITE),
            Map.entry(NamedTextColor.YELLOW, BarColor.YELLOW)
    );

    private ColorHelper() {
        // Not called.
    }

    public static Color convertChatColorToColor(NamedTextColor chatColor) {
        if (chatColor == null) {
            return Color.WHITE;
        }
        return COLOR_MAP.getOrDefault(chatColor, Color.WHITE);
    }

    public static BarColor convertChatColorToBarColor(NamedTextColor chatColor) {
        if (chatColor == null) {
            return BarColor.WHITE;
        }
        return BAR_COLOR_MAP.getOrDefault(chatColor, BarColor.WHITE);
    }

    public static NamedTextColor convertStringToNamedTextColor(String colorCode) {
        if (colorCode == null || colorCode.isEmpty()) {
            return NamedTextColor.WHITE;
        }
        return switch (colorCode) {
            case "0" -> NamedTextColor.BLACK;
            case "1" -> NamedTextColor.DARK_BLUE;
            case "2" -> NamedTextColor.DARK_GREEN;
            case "3" -> NamedTextColor.DARK_AQUA;
            case "4" -> NamedTextColor.DARK_RED;
            case "5" -> NamedTextColor.DARK_PURPLE; // This corresponds to color code "5"
            case "6" -> NamedTextColor.GOLD;
            case "7" -> NamedTextColor.GRAY;
            case "8" -> NamedTextColor.DARK_GRAY;
            case "9" -> NamedTextColor.BLUE;
            case "a" -> NamedTextColor.GREEN;
            case "b" -> NamedTextColor.AQUA;
            case "c" -> NamedTextColor.RED;
            case "d" -> NamedTextColor.LIGHT_PURPLE;
            case "e" -> NamedTextColor.YELLOW;
            case "f" -> NamedTextColor.WHITE;
            default -> NamedTextColor.WHITE; // Default to white if the color code is invalid
        };
    }
}