package com.hm.achievement.command.pagination;

import com.hm.achievement.utils.ColorHelper;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Class for testing the command pagination utility.
 *
 * @author Rsl1122
 */
class CommandPaginationTest {

    private final List<Component> toPaginate = Arrays.asList(Component.text("1"), Component.text("2"), Component.text("3"), Component.text("4"), Component.text("5"), Component.text("6"), Component.text("7"), Component.text("8"), Component.text("9"), Component.text("10"), Component.text("1"), Component.text("2"), Component.text("3"), Component.text("4"), Component.text("5"), Component.text("6"), Component.text("7"), Component.text("8"), Component.text("9"), Component.text("10"), Component.text("1"), Component.text("2"), Component.text("3"), Component.text("4"), Component.text("5"), Component.text("6"), Component.text("7"), Component.text("8"), Component.text("9"), Component.text("10"), Component.text("1"), Component.text("2"), Component.text("3"), Component.text("4"), Component.text("5"), Component.text("6"), Component.text("7"), Component.text("8"), Component.text("9"), Component.text("10"), Component.text("1"), Component.text("2"), Component.text("3"), Component.text("4"), Component.text("5"), Component.text("6"), Component.text("7"), Component.text("8"), Component.text("9"), Component.text("10"), Component.text("1"), Component.text("2"), Component.text("3"), Component.text("4"), Component.text("5"), Component.text("6"), Component.text("7"), Component.text("8"), Component.text("9"), Component.text("10"));

    private YamlConfiguration langConfig;

    @BeforeEach
    void setUp() {
        langConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(Objects.requireNonNull(getClass().getResourceAsStream("/lang.yml"))));
    }

    private @NonNull List<Component> paginate(List<Component> items, int page) {
        List<Component> result = new ArrayList<>();
        new CommandPagination(items, 18, langConfig).sendPage(page, result::add);
        return result;
    }


    @Test
    void testPagination() {
        List<Component> expected = Arrays.asList(getPaginationHeader(1, 4), Component.text("1"), Component.text("2"), Component.text("3"), Component.text("4"), Component.text("5"), Component.text("6"), Component.text("7"), Component.text("8"), Component.text("9"), Component.text("10"), Component.text("1"), Component.text("2"), Component.text("3"), Component.text("4"), Component.text("5"), Component.text("6"), Component.text("7"), Component.text("8"), getPaginationFooter());
        assertEquals(expected, paginate(toPaginate, 1));
    }

    @Test
    void testPaginationPage2() {
        List<Component> expected = Arrays.asList(getPaginationHeader(2, 4), Component.text("9"), Component.text("10"), Component.text("1"), Component.text("2"), Component.text("3"), Component.text("4"), Component.text("5"), Component.text("6"), Component.text("7"), Component.text("8"), Component.text("9"), Component.text("10"), Component.text("1"), Component.text("2"), Component.text("3"), Component.text("4"), Component.text("5"), Component.text("6"), getPaginationFooter());
        assertEquals(expected, paginate(toPaginate, 2));
    }

    @Test
    void testPaginationPage3() {
        List<Component> expected = Arrays.asList(getPaginationHeader(3, 4), Component.text("7"), Component.text("8"), Component.text("9"), Component.text("10"), Component.text("1"), Component.text("2"), Component.text("3"), Component.text("4"), Component.text("5"), Component.text("6"), Component.text("7"), Component.text("8"), Component.text("9"), Component.text("10"), Component.text("1"), Component.text("2"), Component.text("3"), Component.text("4"), getPaginationFooter());
        assertEquals(expected, paginate(toPaginate, 3));
    }

    @Test
    void testPaginationPage4() {
        List<Component> expected = Arrays.asList(getPaginationHeader(4, 4), Component.text("5"), Component.text("6"), Component.text("7"), Component.text("8"), Component.text("9"), Component.text("10"), getPaginationFooter());
        assertEquals(expected, paginate(toPaginate, 4));
    }

    @Test
    void testPaginationPage5WhenOnly4Pages() {
        List<Component> expected = Arrays.asList(getPaginationHeader(4, 4), Component.text("5"), Component.text("6"), Component.text("7"), Component.text("8"), Component.text("9"), Component.text("10"), getPaginationFooter());
        assertEquals(expected, paginate(toPaginate, 5));
    }

    @Test
    void testPaginationPageSinglePage() {
        List<Component> expected = Arrays.asList(getPaginationHeader(1, 1), Component.text("1"), getPaginationFooter());
        assertEquals(expected, paginate(Collections.singletonList(Component.text("1")), 5));
    }

    @Test
    void testEmptyPagination() {
        List<Component> expected = Arrays.asList(getPaginationHeader(0, 0), getPaginationFooter());
        assertEquals(expected, paginate(Collections.emptyList(), 1));
    }

    private @NonNull Component getPaginationHeader(int page, int max) {
        return ColorHelper.convertAmpersandToComponent(StringUtils.replaceEach(langConfig.getString("pagination-header"), new String[]{"PAGE", "MAX"}, new String[]{Integer.toString(page), Integer.toString(max)}));
    }

    private @NonNull Component getPaginationFooter() {
        return ColorHelper.convertAmpersandToComponent(langConfig.getString("pagination-footer"));
    }
}