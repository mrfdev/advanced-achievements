package com.hm.achievement.utils;

/**
 * Simple class providing helper methods to perform simple number operations.
 *
 * @author Pyves
 */
public class NumberHelper {

    private NumberHelper() {
        // Not called.
    }

    /**
     * This simple function gets the smallest multiple of 9 greater than its input value.
     *
     * @param value
     * @return closest multiple of 9 greater than value
     */
    public static int nextMultipleOf9(int value) {
        int multipleOfNine = 9;
        while (multipleOfNine < value) {
            multipleOfNine += 9;
        }
        return multipleOfNine;
    }

}
