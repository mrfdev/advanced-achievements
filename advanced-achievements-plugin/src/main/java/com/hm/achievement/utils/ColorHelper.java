package com.hm.achievement.utils;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Color;
import org.bukkit.boss.BarColor;
import org.jetbrains.annotations.NotNull;

import static net.kyori.adventure.text.format.NamedTextColor.AQUA;
import static net.kyori.adventure.text.format.NamedTextColor.BLACK;
import static net.kyori.adventure.text.format.NamedTextColor.BLUE;
import static net.kyori.adventure.text.format.NamedTextColor.DARK_AQUA;
import static net.kyori.adventure.text.format.NamedTextColor.DARK_BLUE;
import static net.kyori.adventure.text.format.NamedTextColor.DARK_GRAY;
import static net.kyori.adventure.text.format.NamedTextColor.DARK_GREEN;
import static net.kyori.adventure.text.format.NamedTextColor.DARK_PURPLE;
import static net.kyori.adventure.text.format.NamedTextColor.DARK_RED;
import static net.kyori.adventure.text.format.NamedTextColor.GOLD;
import static net.kyori.adventure.text.format.NamedTextColor.GRAY;
import static net.kyori.adventure.text.format.NamedTextColor.GREEN;
import static net.kyori.adventure.text.format.NamedTextColor.LIGHT_PURPLE;
import static net.kyori.adventure.text.format.NamedTextColor.NAMES;
import static net.kyori.adventure.text.format.NamedTextColor.RED;
import static net.kyori.adventure.text.format.NamedTextColor.WHITE;
import static net.kyori.adventure.text.format.NamedTextColor.YELLOW;

public class ColorHelper {

    protected static final List<NamedTextColor> ALL_NAMED_COLORS = List.of(BLACK, DARK_BLUE, DARK_GREEN, DARK_AQUA, DARK_RED, DARK_PURPLE, GOLD, GRAY, DARK_GRAY, BLUE, GREEN, AQUA, RED, LIGHT_PURPLE, YELLOW, WHITE);
    private static final Map<NamedTextColor, Color> COLOR_MAP = Map.ofEntries(Map.entry(AQUA, Color.fromRGB(0x55, 0xFF, 0xFF)), Map.entry(BLACK, Color.BLACK), Map.entry(BLUE, Color.fromRGB(0x55, 0x55, 0xFF)), Map.entry(GRAY, Color.fromRGB(0xAA, 0xAA, 0xAA)), Map.entry(DARK_AQUA, Color.fromRGB(0x00, 0xAA, 0xAA)), Map.entry(DARK_BLUE, Color.fromRGB(0x00, 0x00, 0xAA)), Map.entry(DARK_GRAY, Color.fromRGB(0x55, 0x55, 0x55)), Map.entry(DARK_GREEN, Color.fromRGB(0x00, 0xAA, 0x00)), Map.entry(DARK_PURPLE, Color.fromRGB(0xAA, 0x00, 0xAA)), Map.entry(DARK_RED, Color.fromRGB(0xAA, 0x00, 0x00)), Map.entry(GOLD, Color.fromRGB(0xFF, 0xAA, 0x00)), Map.entry(GREEN, Color.fromRGB(0x55, 0xFF, 0x55)), Map.entry(LIGHT_PURPLE, Color.fromRGB(0xFF, 0x55, 0xFF)), Map.entry(RED, Color.fromRGB(0xFF, 0x55, 0x55)), Map.entry(WHITE, Color.WHITE), Map.entry(YELLOW, Color.fromRGB(0xFF, 0xFF, 0x55)));
    private static final Map<NamedTextColor, BarColor> BAR_COLOR_MAP = Map.ofEntries(Map.entry(AQUA, BarColor.GREEN), Map.entry(BLACK, BarColor.PURPLE), Map.entry(BLUE, BarColor.BLUE), Map.entry(GRAY, BarColor.WHITE), Map.entry(DARK_AQUA, BarColor.BLUE), Map.entry(DARK_BLUE, BarColor.BLUE), Map.entry(DARK_GRAY, BarColor.PURPLE), Map.entry(DARK_GREEN, BarColor.GREEN), Map.entry(DARK_PURPLE, BarColor.PURPLE), Map.entry(DARK_RED, BarColor.RED), Map.entry(GOLD, BarColor.YELLOW), Map.entry(GREEN, BarColor.GREEN), Map.entry(LIGHT_PURPLE, BarColor.PURPLE), Map.entry(RED, BarColor.RED), Map.entry(WHITE, BarColor.WHITE), Map.entry(YELLOW, BarColor.YELLOW));
    private static final Map<Integer, NamedTextColor> EXACT_RGB_MAP = ALL_NAMED_COLORS.stream().collect(Collectors.toMap(c -> (c.red() << 16) | (c.green() << 8) | c.blue(), c -> c));

    private ColorHelper() {
        // Not called.
    }

    public static Color convertChatColorToColor(NamedTextColor chatColor) {
        return COLOR_MAP.getOrDefault(chatColor, Color.WHITE);
    }

    public static BarColor convertChatColorToBarColor(NamedTextColor chatColor) {
        return BAR_COLOR_MAP.getOrDefault(chatColor, BarColor.WHITE);
    }

    public static NamedTextColor parseColor(String nameorHex) {
        if (nameorHex == null) return WHITE;
        try { // TODO: Forcing 5 to be dark_purple for config as that uses numbers. Remove at some point
            int index = Integer.parseInt(nameorHex);
            if (index == 5) {
                return DARK_PURPLE;
            }
        } catch (NumberFormatException ignored) {
        }
        NamedTextColor named = NAMES.value(nameorHex.toLowerCase(Locale.ROOT));
        if (named != null) return named;
        try {
            int rgb = Integer.parseInt(nameorHex.replace("#", ""), 16);
            NamedTextColor exact = EXACT_RGB_MAP.get(rgb);
            if (exact != null) return exact;
            return nearestTo(Color.fromRGB(rgb));
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid color: " + nameorHex, e);
        }
    }

    private static @NotNull NamedTextColor nearestTo(@NotNull Color color) {
        TextColor advColor = TextColor.color(color.getRed(), color.getGreen(), color.getBlue());
        return NamedTextColor.nearestTo(advColor);
    }
}