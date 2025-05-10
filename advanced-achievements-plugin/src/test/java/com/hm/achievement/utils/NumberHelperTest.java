package com.hm.achievement.utils;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class NumberHelperTest {

    @Test
    void shouldComputeNextMultipleOf9() {
        assertEquals(18, NumberHelper.nextMultipleOf9(17));
        assertEquals(18, NumberHelper.nextMultipleOf9(18));
        assertEquals(27, NumberHelper.nextMultipleOf9(19));
    }

}
