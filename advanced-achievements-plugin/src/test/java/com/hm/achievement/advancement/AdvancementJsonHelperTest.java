package com.hm.achievement.advancement;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.hm.achievement.advancement.AchievementAdvancement.AchievementAdvancementBuilder;

/**
 * Class for testing advancement JSON strings.
 *
 * @author Pyves
 */
class AdvancementJsonHelperTest {

	@Test
	void shouldGenerateAdvancementJson() {
		AchievementAdvancement aa = new AchievementAdvancementBuilder().iconItem("minecraft:dirt")
				.iconData("0").title("Special Event Achievement!").description("You took part in the \"Special Event\"!")
				.parent("advancedachievements:advanced_achievements_parent").type(AdvancementType.TASK).build();

		assertEquals("""
                {
                  "criteria":{
                    "aach_handled":{
                      "trigger":"minecraft:impossible"
                    }
                  },
                  "requirements":[
                    [
                      "aach_handled"
                    ]
                  ],
                  "display":{
                    "icon":{
                      "id":"minecraft:dirt","data":0
                    },
                    "title":"Special Event Achievement!",
                    "description":"You took part in the \\"Special Event\\"!",
                    "frame":"task",
                    "announce_to_chat":false
                  },
                  "parent":"advancedachievements:advanced_achievements_parent"
                }
                """, AdvancementJsonHelper.toJson(aa));
	}

	@Test
	void shouldGenerateParentAdvancementJson() {
		AchievementAdvancement aa = new AchievementAdvancementBuilder().iconItem("minecraft:dirt")
				.iconData("0").title("Special Event Achievement!").description("You took part in the special event!")
				.background("minecraft:book").type(AdvancementType.GOAL).build();

		assertEquals("""
                {
                  "criteria":{
                    "aach_handled":{
                      "trigger":"minecraft:impossible"
                    }
                  },
                  "requirements":[
                    [
                      "aach_handled"
                    ]
                  ],
                  "display":{
                    "icon":{
                      "id":"minecraft:dirt","data":0
                    },
                    "title":"Special Event Achievement!",
                    "description":"You took part in the special event!",
                    "frame":"goal",
                    "announce_to_chat":false,
                    "background":"minecraft:book"
                  }
                }
                """, AdvancementJsonHelper.toJson(aa));
	}

	@Test
	void shouldGenerateHiddenParentAdvancementJson() {
		assertEquals("""
                {
                  "criteria":{
                    "aach_handled":{
                      "trigger":"minecraft:impossible"
                    }
                  },
                  "requirements":[
                    [
                      "aach_handled"
                    ]
                  ],
                  "background":"minecraft:book"
                }
                """, AdvancementJsonHelper.toHiddenJson("minecraft:book"));
	}

}
