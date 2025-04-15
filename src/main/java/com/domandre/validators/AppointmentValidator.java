package com.domandre.validators;

import com.domandre.helpers.BusinessHoursHelper;
import com.domandre.helpers.BusinessHoursHelper.TimeRange;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

public class AppointmentValidator {
    public static boolean isValidAppointment(LocalDateTime dateTime) {
        DayOfWeek day = dateTime.getDayOfWeek();
        LocalTime time = dateTime.toLocalTime();

        Optional<TimeRange> rangeOptional = BusinessHoursHelper.getBusinessHours(day);

        if (rangeOptional.isEmpty()) {
            return false;
        }

        TimeRange range = rangeOptional.get();
        LocalTime lastAllowed = range.end().minusHours(1);

        return !time.isBefore(range.start()) &&
                !time.isAfter(lastAllowed) &&
                dateTime.isAfter(LocalDateTime.now());
    }
}
