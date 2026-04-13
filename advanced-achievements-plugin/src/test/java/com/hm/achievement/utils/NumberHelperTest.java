package com.hm.achievement.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NumberHelperTest {

    @Test
    void shouldComputeNextMultipleOf9() {
        assertEquals(9, NumberHelper.nextMultipleOf9(1));
        assertEquals(9, NumberHelper.nextMultipleOf9(9));
        assertEquals(18, NumberHelper.nextMultipleOf9(10));
        assertEquals(18, NumberHelper.nextMultipleOf9(18));
        assertEquals(27, NumberHelper.nextMultipleOf9(19));
        assertEquals(27, NumberHelper.nextMultipleOf9(27));
        assertEquals(36, NumberHelper.nextMultipleOf9(28));
        assertEquals(36, NumberHelper.nextMultipleOf9(36));
    }

    @Test
    void shouldHandleZero() {
        assertEquals(0, NumberHelper.nextMultipleOf9(0));
    }

    @Test
    void shouldHandleExactMultiples() {
        assertEquals(9, NumberHelper.nextMultipleOf9(9));
        assertEquals(27, NumberHelper.nextMultipleOf9(27));
        assertEquals(90, NumberHelper.nextMultipleOf9(90));
    }

    @Test
    void shouldHandleNegativeNumbers() {
        assertEquals(0, NumberHelper.nextMultipleOf9(-1));
        assertEquals(0, NumberHelper.nextMultipleOf9(-8));
        assertEquals(-9, NumberHelper.nextMultipleOf9(-9));
        assertEquals(-9, NumberHelper.nextMultipleOf9(-10));
    }
}
