package com.hm.achievement.db.data;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.jetbrains.annotations.NotNull;

public record ConnectionInformation(String date, long count) {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public ConnectionInformation() {
        this(epoch(), 0L);
    }

    public static @NotNull String epoch() {
        return LocalDate.ofEpochDay(0).format(DATE_TIME_FORMATTER);
    }

    public static @NotNull String today() {
        return LocalDate.now().format(DATE_TIME_FORMATTER);
    }

}
