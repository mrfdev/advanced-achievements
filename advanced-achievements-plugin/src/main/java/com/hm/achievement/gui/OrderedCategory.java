package com.hm.achievement.gui;

import com.hm.achievement.category.Category;
import org.jetbrains.annotations.NotNull;

/**
 * Small wrapper to define an ordering between the categories.
 *
 * @author Pyves
 */
public record OrderedCategory(int order, Category category) implements Comparable<OrderedCategory> {


    @Override
    public int compareTo(@NotNull OrderedCategory o) {
        return Integer.compare(order(), o.order());
    }
}
