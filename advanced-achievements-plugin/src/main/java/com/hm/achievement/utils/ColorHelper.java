package com.hm.achievement.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jspecify.annotations.NonNull;

public class ColorHelper {

    public static NamedTextColor parseColor(String color) {
        if (color == null) return NamedTextColor.WHITE;
        NamedTextColor named = NamedTextColor.NAMES.value(color.toLowerCase());
        if (named != null) return named;
        try {
            int rgb = Integer.parseInt(color.replace("#", ""), 16);
            return NamedTextColor.nearestTo(TextColor.color(rgb));
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid color: " + color, e);
        }
    }

    public static NamedTextColor configColor(@NonNull YamlConfiguration mainConfig) {
        return parseColor(mainConfig.getString("Color", "DARK_PURPLE"));
    }

    public static @NonNull Component convertAmpersandToComponent(String text) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(text);
    }

    public static @NonNull String namedTextColorToLegacyAmpersand(NamedTextColor color) {
        return LegacyComponentSerializer.legacyAmpersand().serialize(Component.empty().color(color)).replace("%r", "");
    }
}