package com.hm.achievement.utils;

import java.util.Arrays;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

/**
 * Class for testing the text helpers.
 *
 * @author Pyves
 */
@ExtendWith(MockitoExtension.class)
class StringHelperTest {

    @Mock
    private Player player;
    @Mock
    private World world;

    @Test
    void shouldRemoveFormattingCodes() {
        String result = StringHelper.removeFormattingCodes("&0&1§2&3&4&5&6&7&8&9This &a&b§c&d&eis&f &ksome&l&m&n&o te&rxt!");

        assertEquals("This is some text!", result);
    }

    @Test
    void shouldNotRemoveInvalidFormattingCodes() {
        String result = StringHelper.removeFormattingCodes("Incorrect formatting codes: &h& z");

        assertEquals("Incorrect formatting codes: &h& z", result);
    }

    @Test
    void shouldReturnClosestMatchingString() {
        List<String> possibleMatches = Arrays.asList("nothing", "something", "random text", "amazing");
        String result = StringHelper.getClosestMatch("somaeThing", possibleMatches);

        assertEquals("something", result);
    }

    @Test
    void shouldReplacePlayerPlaceholders() {
        when(player.getName()).thenReturn("Pyves");
        when(player.getLocation()).thenReturn(new Location(world, 1, 5, 8));
        when(player.getWorld()).thenReturn(world);
        when(world.getName()).thenReturn("Nether");

        Component result = StringHelper.replacePlayerPlaceholders(
                "Player PLAYER is in the PLAYER_WORLD at position PLAYER_X PLAYER_Y PLAYER_Z", player);

        String resultString = PlainTextComponentSerializer.plainText().serialize(result);

        assertEquals("Player Pyves is in the Nether at position 1 5 8", resultString);
    }

    @Test
    void unescapeJavaNullInput() {
        assertNull(StringHelper.unescapeJava(null));
    }

    @Test
    void unescapeJavaConvertUnicodeEscapes() {
        String input = "Hello\\u0020World\\u0021";
        String expected = "Hello World!";
        assertEquals(expected, StringHelper.unescapeJava(input));
    }

    @Test
    void unescapeJavaLeavesInvalidEscapes() {
        String input = "Invalid \\uZZZZ escape";
        String expected = "Invalid \\uZZZZ escape";
        assertEquals(expected, StringHelper.unescapeJava(input));
    }


    @Test
    void escapeJson_shouldReturnNullForNullInput() {
        assertNull(StringHelper.escapeJson(null));
    }

    @Test
    void escapeJson_shouldEscapeSpecialCharacters() {
        String input = "Quotes: \", Backslash: \\, Newline:\n, Tab:\t";
        String expected = "Quotes: \\\", Backslash: \\\\, Newline:\\n, Tab:\\t";
        assertEquals(expected, StringHelper.escapeJson(input));
    }

    @Test
    void escapeJson_shouldEscapeControlCharacters() {
        String input = "Control:\u0001\u0002";
        String expected = "Control:\\u0001\\u0002";
        assertEquals(expected, StringHelper.escapeJson(input));
    }

    @Test
    void levenshteinDistance_shouldReturnCorrectDistance() {
        assertEquals(0, StringHelper.levenshteinDistance("test", "test"));
        assertEquals(1, StringHelper.levenshteinDistance("test", "tent"));
        assertEquals(4, StringHelper.levenshteinDistance("test", "abcd"));
        assertEquals(4, StringHelper.levenshteinDistance("", "abcd"));
        assertEquals(0, StringHelper.levenshteinDistance("", ""));
    }
}
