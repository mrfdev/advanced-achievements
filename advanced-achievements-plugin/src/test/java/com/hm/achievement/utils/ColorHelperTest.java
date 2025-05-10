package com.hm.achievement.utils;

import java.util.Map;
import java.util.stream.Stream;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Color;
import org.bukkit.boss.BarColor;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ColorHelperTest {
    private static final Map<NamedTextColor, BarColor> EXPECTED_BAR_COLORS = Map.ofEntries(
            Map.entry(NamedTextColor.BLACK, BarColor.PURPLE),
            Map.entry(NamedTextColor.DARK_BLUE, BarColor.BLUE),
            Map.entry(NamedTextColor.DARK_GREEN, BarColor.GREEN),
            Map.entry(NamedTextColor.DARK_AQUA, BarColor.BLUE),
            Map.entry(NamedTextColor.DARK_RED, BarColor.RED),
            Map.entry(NamedTextColor.DARK_PURPLE, BarColor.PURPLE),
            Map.entry(NamedTextColor.GOLD, BarColor.YELLOW),
            Map.entry(NamedTextColor.GRAY, BarColor.WHITE),
            Map.entry(NamedTextColor.DARK_GRAY, BarColor.PURPLE),
            Map.entry(NamedTextColor.BLUE, BarColor.BLUE),
            Map.entry(NamedTextColor.GREEN, BarColor.GREEN),
            Map.entry(NamedTextColor.AQUA, BarColor.GREEN),
            Map.entry(NamedTextColor.RED, BarColor.RED),
            Map.entry(NamedTextColor.LIGHT_PURPLE, BarColor.PURPLE),
            Map.entry(NamedTextColor.YELLOW, BarColor.YELLOW),
            Map.entry(NamedTextColor.WHITE, BarColor.WHITE)
    );

    static @NotNull Stream<NamedTextColor> allNamedColors() {
        return NamedTextColor.NAMES.values().stream();
    }

    static @NotNull Stream<Arguments> namedTextColorAndBarColor() {
        return EXPECTED_BAR_COLORS.entrySet().stream().map(entry -> Arguments.of(entry.getKey(), entry.getValue()));
    }

    @ParameterizedTest
    @MethodSource("allNamedColors")
    void shouldConvertNamedTextColorstoColor(NamedTextColor namedTextColor) {
        Color color = ColorHelper.convertChatColorToColor(namedTextColor);
        Assertions.assertEquals(Color.fromRGB(namedTextColor.red(), namedTextColor.green(), namedTextColor.blue()), color);
    }

    @ParameterizedTest
    @MethodSource("namedTextColorAndBarColor")
    void shouldConvertNamedTextColorTobarColor(NamedTextColor namedTextColor, BarColor expectedBarColor) {
        BarColor barColor = ColorHelper.convertChatColorToBarColor(namedTextColor);
        Assertions.assertEquals(expectedBarColor, barColor);
    }
}