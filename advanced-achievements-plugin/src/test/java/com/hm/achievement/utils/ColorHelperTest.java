package com.hm.achievement.utils;

import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.util.Index;
import org.bukkit.Color;
import org.bukkit.boss.BarColor;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

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
import static net.kyori.adventure.text.format.NamedTextColor.RED;
import static net.kyori.adventure.text.format.NamedTextColor.WHITE;
import static net.kyori.adventure.text.format.NamedTextColor.YELLOW;

class ColorHelperTest {

    static @NotNull Stream<Arguments> namedAndBarColors() {
        return Stream.of(Arguments.of(BLACK, BarColor.PURPLE), Arguments.of(DARK_BLUE, BarColor.BLUE), Arguments.of(DARK_GREEN, BarColor.GREEN), Arguments.of(DARK_AQUA, BarColor.BLUE), Arguments.of(DARK_RED, BarColor.RED), Arguments.of(DARK_PURPLE, BarColor.PURPLE), Arguments.of(GOLD, BarColor.YELLOW), Arguments.of(GRAY, BarColor.WHITE), Arguments.of(DARK_GRAY, BarColor.PURPLE), Arguments.of(BLUE, BarColor.BLUE), Arguments.of(GREEN, BarColor.GREEN), Arguments.of(AQUA, BarColor.GREEN), Arguments.of(RED, BarColor.RED), Arguments.of(LIGHT_PURPLE, BarColor.PURPLE), Arguments.of(YELLOW, BarColor.YELLOW), Arguments.of(WHITE, BarColor.WHITE));
    }

    static @NotNull Stream<Arguments> namedColors() {
        return Stream.of(Arguments.of(BLACK), Arguments.of(DARK_BLUE), Arguments.of(DARK_GREEN), Arguments.of(DARK_AQUA), Arguments.of(DARK_RED), Arguments.of(DARK_PURPLE), Arguments.of(GOLD), Arguments.of(GRAY), Arguments.of(DARK_GRAY), Arguments.of(BLUE), Arguments.of(GREEN), Arguments.of(AQUA), Arguments.of(RED), Arguments.of(LIGHT_PURPLE), Arguments.of(YELLOW), Arguments.of(WHITE));
    }

    static @NotNull Stream<Arguments> invalidColorInputs() {
        return Stream.of(Arguments.of("not_a_color"), Arguments.of("#GGGGGG"), Arguments.of(""));
    }

    @ParameterizedTest
    @MethodSource("namedAndBarColors")
    void shouldConvertNamedTextColorToBarColor(NamedTextColor namedTextColor, BarColor expectedBarColor) {
        BarColor actualBarColor = ColorHelper.convertChatColorToBarColor(namedTextColor);
        Assertions.assertEquals(expectedBarColor, actualBarColor);
    }

    @ParameterizedTest
    @MethodSource("namedColors")
    void shouldConvertNamedTextColorToBukkitCOlor(@NotNull NamedTextColor namedTextColor) {
        Color expectedColor = Color.fromRGB(namedTextColor.red(), namedTextColor.green(), namedTextColor.blue());
        Color actualColor = ColorHelper.convertChatColorToColor(namedTextColor);
        Assertions.assertEquals(expectedColor, actualColor);
    }
    @ParameterizedTest
    @MethodSource("invalidColorInputs")
    void shouldThrowOnInvalidColor(String input) {
        Assertions.assertThrows(RuntimeException.class, () -> ColorHelper.parseColor(input));
    }

    @ParameterizedTest
    @MethodSource("namedColors")
    void shouldParseNamedTextColorCaseInsensitive(@NotNull NamedTextColor namedTextColor) {
        String mixedCase = namedTextColor.toString().substring(0, 1).toUpperCase(Locale.ROOT) + namedTextColor.toString().substring(1).toLowerCase(Locale.ROOT);
        NamedTextColor actual = ColorHelper.parseColor(mixedCase);
        Assertions.assertEquals(namedTextColor, actual);
    }

    @Test
    void shouldReturnWhiteForUnknownNamedTextColor() {
        NamedTextColor namedTextColor = Mockito.mock(NamedTextColor.class);
        Mockito.when(namedTextColor.red()).thenReturn(123);
        Mockito.when(namedTextColor.green()).thenReturn(123);
        Mockito.when(namedTextColor.blue()).thenReturn(123);
        Color color = ColorHelper.convertChatColorToColor(namedTextColor);
        Assertions.assertEquals(Color.WHITE, color);
    }

    @Test
    void shouldReturnWhiteBarColorForUnknownNamedTextColor() {
        NamedTextColor namedTextColor = Mockito.mock(NamedTextColor.class);
        Mockito.when(namedTextColor.red()).thenReturn(123);
        Mockito.when(namedTextColor.green()).thenReturn(123);
        Mockito.when(namedTextColor.blue()).thenReturn(123);
        BarColor barColor = ColorHelper.convertChatColorToBarColor(namedTextColor);
        Assertions.assertEquals(BarColor.WHITE, barColor);
    }

    @Test
    void shouldReturnWhiteWhenParsingNull() {
        Assertions.assertEquals(WHITE, ColorHelper.parseColor(null));
    }

    @Test
    void namedTextColorsArePresentinIndex() {
        Index<String, NamedTextColor> namesIndex = NamedTextColor.NAMES;
        Set<String> keys = namesIndex.keys();
        Set<String> expectedNames = ColorHelper.ALL_NAMED_COLORS.stream()
                .map(c -> c.toString().toLowerCase())
                .collect(Collectors.toSet());
        Assertions.assertTrue(keys.containsAll(expectedNames), "NAMES index should contain all expected colors");
    }
}