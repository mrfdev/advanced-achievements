package com.hm.achievement.utils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jspecify.annotations.NonNull;

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
    private static final Map<Integer, NamedTextColor> EXACT_RGB_MAP = ALL_NAMED_COLORS.stream().collect(Collectors.toMap(c -> (c.red() << 16) | (c.green() << 8) | c.blue(), c -> c));

    static NamedTextColor parseColor(String color) {
        if (color == null) return WHITE;
        NamedTextColor named = NAMES.value(color.toLowerCase());
        if (named != null) return named;
        try {
            int rgb = Integer.parseInt(color.replace("#", ""), 16);
            NamedTextColor exact = EXACT_RGB_MAP.get(rgb);
            if (exact != null) return exact;
            return NamedTextColor.nearestTo(TextColor.color(rgb));
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid color: " + color, e);
        }
    }

    public static NamedTextColor configColor(@NonNull YamlConfiguration mainConfig) {
        return parseColor(mainConfig.getString("Color", "DARK_PURPLE"));
    }

    public static NamedTextColor configFireworkColor(@NonNull YamlConfiguration mainConfig) {
        return parseColor(mainConfig.getString("FireworkColor", "DARK_PURPLE"));
    }
}