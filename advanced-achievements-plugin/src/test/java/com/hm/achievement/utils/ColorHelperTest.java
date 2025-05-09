package com.hm.achievement.utils;

import java.util.stream.Stream;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.boss.BarColor;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;
import org.junit.jupiter.params.provider.MethodSource;

class ColorHelperTest {

	@ParameterizedTest
	@EnumSource(value = ChatColor.class, mode = Mode.EXCLUDE, names = { "MAGIC", "BOLD", "STRIKETHROUGH", "UNDERLINE",
			"ITALIC", "RESET" })
	void shouldConvertChatColorsToColor(ChatColor chatColor) {
		Color color = ColorHelper.convertChatColorToColor(chatColor);
		NamedTextColor namedTextColor = NamedTextColor.NAMES.value(chatColor.name().toLowerCase());
		assert namedTextColor != null;
		Assertions.assertEquals(Color.fromRGB(namedTextColor.red(), namedTextColor.green(), namedTextColor.blue()), color);
	}

	@ParameterizedTest
	@EnumSource(value = ChatColor.class, mode = Mode.INCLUDE, names = { "MAGIC", "BOLD", "STRIKETHROUGH", "UNDERLINE",
			"ITALIC", "RESET" })
	void shouldConvertChatFormatsToWhite(ChatColor chatColor) {
		Color color = ColorHelper.convertChatColorToColor(chatColor);

		Assertions.assertEquals(Color.WHITE, color);
	}

	@ParameterizedTest
	@MethodSource("chatAndBarColors")
	void shouldConvertChatColorToBarColor(ChatColor chatColor, BarColor barColor) {
		BarColor color = ColorHelper.convertChatColorToBarColor(chatColor);

		Assertions.assertEquals(barColor, color);
	}

	static @NotNull Stream<Arguments> chatAndBarColors() {
		return Stream.of(
				Arguments.of(ChatColor.BLACK, BarColor.PURPLE),
				Arguments.of(ChatColor.DARK_BLUE, BarColor.BLUE),
				Arguments.of(ChatColor.DARK_GREEN, BarColor.GREEN),
				Arguments.of(ChatColor.DARK_AQUA, BarColor.BLUE),
				Arguments.of(ChatColor.DARK_RED, BarColor.RED),
				Arguments.of(ChatColor.DARK_PURPLE, BarColor.PURPLE),
				Arguments.of(ChatColor.GOLD, BarColor.YELLOW),
				Arguments.of(ChatColor.GRAY, BarColor.WHITE),
				Arguments.of(ChatColor.DARK_GRAY, BarColor.PURPLE),
				Arguments.of(ChatColor.BLUE, BarColor.BLUE),
				Arguments.of(ChatColor.GREEN, BarColor.GREEN),
				Arguments.of(ChatColor.AQUA, BarColor.GREEN),
				Arguments.of(ChatColor.RED, BarColor.RED),
				Arguments.of(ChatColor.LIGHT_PURPLE, BarColor.PURPLE),
				Arguments.of(ChatColor.YELLOW, BarColor.YELLOW),
				Arguments.of(ChatColor.WHITE, BarColor.WHITE),
				Arguments.of(ChatColor.MAGIC, BarColor.WHITE),
				Arguments.of(ChatColor.BOLD, BarColor.WHITE),
				Arguments.of(ChatColor.STRIKETHROUGH, BarColor.WHITE),
				Arguments.of(ChatColor.UNDERLINE, BarColor.WHITE),
				Arguments.of(ChatColor.ITALIC, BarColor.WHITE),
				Arguments.of(ChatColor.RESET, BarColor.WHITE));
	}

}