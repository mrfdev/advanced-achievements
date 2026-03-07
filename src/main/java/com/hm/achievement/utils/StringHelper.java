package com.hm.achievement.utils;

import java.util.Collection;
import java.util.regex.Pattern;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;

/**
 * Simple class providing helper methods to process strings.
 *
 * @author Pyves
 */
public class StringHelper {

    private static final Pattern FORMATTING_CODE_PATTERN = Pattern.compile("([&§])([a-f]|r|[k-o]|[0-9])");

    private StringHelper() {
        // Not called.
    }

    public static String removeFormattingCodes(String text) {
        return FORMATTING_CODE_PATTERN.matcher(text).replaceAll("");
    }

    public static String getClosestMatch(String toMatch, @NonNull Collection<String> possibleMatches) {
        int smallestDistance = Integer.MAX_VALUE;
        String closestMatch = "";
        for (String possibleMatch : possibleMatches) {
            int distance = levenshteinDistance(toMatch, possibleMatch);
            if (distance < smallestDistance) {
                smallestDistance = distance;
                closestMatch = possibleMatch;
            }
        }
        return closestMatch;
    }

    public static @NonNull Component replacePlayerPlaceholders(Object input, @NonNull Player player) {
        String str;
        if (input instanceof Component) {
            str = input.toString();
        } else if (input instanceof String) {
            str = (String) input;
        } else {
            throw new IllegalStateException("Input must be string or component");
        }
        str = StringUtils.replaceEach(str, new String[]{"PLAYER_WORLD", "PLAYER_X", "PLAYER_Y", "PLAYER_Z", "PLAYER"}, new String[]{player.getWorld().getName(), Integer.toString(player.getLocation().getBlockX()), Integer.toString(player.getLocation().getBlockY()), Integer.toString(player.getLocation().getBlockZ()), player.getName()});
        return Component.text(str);
    }

    public static @NonNull String toReadableName(@NonNull Material material) {
        String[] words = material.name().toLowerCase().split("_");
        for (int i = 0; i < words.length; i++) words[i] = Character.toUpperCase(words[i].charAt(0)) + words[i].substring(1);
        return String.join(" ", words);
    }

    @Contract("null -> null")
    public static String unescapeJava(String input) {
        if (input == null) return null;
        StringBuilder sb = new StringBuilder();
        int length = input.length();
        for (int i = 0; i < length; i++) {
            char c = input.charAt(i);
            if (c == '\\' && i + 5 < length && input.charAt(i + 1) == 'u') {
                String hex = input.substring(i + 2, i + 6);
                try {
                    int code = Integer.parseInt(hex, 16);
                    sb.append((char) code);
                    i += 5;
                } catch (NumberFormatException e) {
                    sb.append(c);
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    @Contract("null -> null")
    public static String escapeJson(String input) {
        if (input == null) return null;
        StringBuilder sb = new StringBuilder(input.length());
        for (char c : input.toCharArray()) {
            switch (c) {
                case '"':
                    sb.append("\\\"");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                case '\b':
                    sb.append("\\b");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.toString();
    }

    public static int levenshteinDistance(@NonNull CharSequence lhs, @NonNull CharSequence rhs) {
        int n = lhs.length();
        int m = rhs.length();
        if (n == 0) return m;
        if (m == 0) return n;
        int[] prev = new int[m + 1];
        int[] curr = new int[m + 1];
        for (int j = 0; j <= m; j++) prev[j] = j;
        for (int i = 1; i <= n; i++) {
            curr[0] = i;
            for (int j = 1; j <= m; j++) {
                int cost = lhs.charAt(i - 1) == rhs.charAt(j - 1) ? 0 : 1;
                curr[j] = Math.min(Math.min(curr[j - 1] + 1, prev[j] + 1), prev[j - 1] + cost);
            }
            int[] temp = prev;
            prev = curr;
            curr = temp;
        }
        return prev[m];
    }
}
