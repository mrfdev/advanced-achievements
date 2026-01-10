package com.hm.achievement.advancement;

import com.hm.achievement.utils.StringHelper;
import org.apache.commons.lang3.StringUtils;

import static com.hm.achievement.advancement.AchievementAdvancement.CRITERIA_NAME;

/**
 * Class used to generate JSON strings for advancements.
 *
 * @author Pyves
 */
public class AdvancementJsonHelper {

    private AdvancementJsonHelper() {
        // Not called.
    }

    public static String toJson(AchievementAdvancement aa) {
        return "{\n" + "  \"criteria\":{\n" + "    \"" + CRITERIA_NAME + "\":{\n" + "      \"trigger\":\"minecraft:impossible\"\n" + "    }\n" + "  },\n" + "  \"requirements\":[\n" + "    [\n" + "      \"" + CRITERIA_NAME + "\"\n" + "    ]\n" + "  ],\n" + "  \"display\":{\n" + "    \"icon\":{\n" + "      \"id\":\"" + aa.getIconItem() + "\"" + getIntegerFieldOrEmpty(aa.getIconData()) + "\n" + "    },\n" + "    \"title\":\"" + StringHelper.escapeJson(aa.getTitle()) + "\",\n" + "    \"description\":\"" + StringHelper.escapeJson(aa.getDescription()) + "\",\n" + "    \"frame\":\"" + aa.getFrame() + "\",\n" + "    \"announce_to_chat\":false" + getStringFieldOrLineBreak("background", aa.getBackground(), 4) + "  }" + getStringFieldOrLineBreak("parent", aa.getParent(), 2) + "}\n";
    }

    public static String toHiddenJson(String background) {
        return "{\n" + "  \"criteria\":{\n" + "    \"" + CRITERIA_NAME + "\":{\n" + "      \"trigger\":\"minecraft:impossible\"\n" + "    }\n" + "  },\n" + "  \"requirements\":[\n" + "    [\n" + "      \"" + CRITERIA_NAME + "\"\n" + "    ]\n" + "  ],\n" + "  \"background\":\"" + background + "\"\n" + "}\n";
    }

    private static String getIntegerFieldOrEmpty(String value) {
        return value == null ? "" : ",\"" + "data" + "\":" + value;
    }

    private static String getStringFieldOrLineBreak(String key, String value, int spacing) {
        return value == null ? "\n" : ",\n" + StringUtils.repeat(' ', spacing) + "\"" + key + "\":\"" + value + "\"\n";
    }

}
