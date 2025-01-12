package com.hm.achievement.domain;

import java.util.UUID;

/**
 * Class linking an {@link Achievement} with a player UUID and an awarded date (number in milliseconds representing the
 * difference between the awarded time and midnight, January 1, 1970 UTC).
 *
 * @author Pyves
 */
public record AwardedAchievement(Achievement achievement, UUID player, long awardedDate) {


}
