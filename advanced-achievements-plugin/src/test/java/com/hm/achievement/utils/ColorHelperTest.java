package com.hm.achievement.utils;

import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.util.Index;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

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

    static @NonNull Stream<Arguments> namedColors() {
        return Stream.of(Arguments.of(BLACK), Arguments.of(DARK_BLUE), Arguments.of(DARK_GREEN), Arguments.of(DARK_AQUA), Arguments.of(DARK_RED), Arguments.of(DARK_PURPLE), Arguments.of(GOLD), Arguments.of(GRAY), Arguments.of(DARK_GRAY), Arguments.of(BLUE), Arguments.of(GREEN), Arguments.of(AQUA), Arguments.of(RED), Arguments.of(LIGHT_PURPLE), Arguments.of(YELLOW), Arguments.of(WHITE));
    }

    static @NonNull Stream<Arguments> invalidColorInputs() {
        return Stream.of(Arguments.of("not_a_color"), Arguments.of("#GGGGGG"), Arguments.of(""));
    }

    @ParameterizedTest
    @MethodSource("invalidColorInputs")
    void throwOnInvalidColor(String input) {
        Assertions.assertThrows(RuntimeException.class, () -> ColorHelper.parseColor(input));
    }

    @ParameterizedTest
    @MethodSource("namedColors")
    void parseNamedTextColorCaseInsensitive(@NonNull NamedTextColor namedTextColor) {
        String mixedCase = namedTextColor.toString().substring(0, 1).toUpperCase(Locale.ROOT) + namedTextColor.toString().substring(1).toLowerCase(Locale.ROOT);
        NamedTextColor actual = ColorHelper.parseColor(mixedCase);
        Assertions.assertEquals(namedTextColor, actual);
    }

    @Test
    void returnWhiteWhenParsingNull() {
        Assertions.assertEquals(WHITE, ColorHelper.parseColor(null));
    }

    @Test
    void namedTextColorsArePresentInIndex() {
        Index<String, NamedTextColor> namesIndex = NamedTextColor.NAMES;
        Set<String> keys = namesIndex.keys();
        Assertions.assertTrue(keys.isEmpty(), "NAMES index should contain all expected colors");
    }

    @Test
    void parseHexColor() {
        Assertions.assertEquals(AQUA, ColorHelper.parseColor("#55FFFF"));
    }

    @Test
    void parseHexColorWithoutHash() {
        Assertions.assertEquals(AQUA, ColorHelper.parseColor("55FFFF"));
    }

    @Test
    void returnNearestNamedColorForApproximateHex() {
        Assertions.assertEquals(RED, ColorHelper.parseColor("#FF5556"));
    }

    @Test
    void returnConfiguredColor() {
        YamlConfiguration config = new YamlConfiguration();
        config.set("Color", "RED");
        Assertions.assertEquals(RED, ColorHelper.configColor(config));
    }

    @Test
    void returnsDarkPurpleAsDefaultConfigColor() {
        YamlConfiguration config = new YamlConfiguration();
        Assertions.assertEquals(DARK_PURPLE, ColorHelper.configColor(config));
    }
}