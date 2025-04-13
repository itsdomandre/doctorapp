package com.domandre.helpers;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Optional;

public class BusinessHoursHelper {

    public static Optional<TimeRange> getBusinessHours(DayOfWeek day) {
        return switch (day) {
            case MONDAY, THURSDAY -> Optional.of(new TimeRange(LocalTime.of(12, 0), LocalTime.of(18, 0)));
            case TUESDAY, WEDNESDAY -> Optional.of(new TimeRange(LocalTime.of(8, 0), LocalTime.of(20, 0)));
            case FRIDAY -> Optional.of(new TimeRange(LocalTime.of(8, 0), LocalTime.of(18, 0)));
            case SUNDAY -> Optional.of(new TimeRange(LocalTime.of(11, 0), LocalTime.of(16, 0)));
            default -> Optional.empty();
        };
    }

    public static boolean isBusinessDay(DayOfWeek day) {
        return getBusinessHours(day).isPresent();
    }

    public record TimeRange(LocalTime start, LocalTime end) {}
}

