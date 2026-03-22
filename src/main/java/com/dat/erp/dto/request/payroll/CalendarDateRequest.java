package com.dat.erp.dto.request.payroll;

import java.time.LocalDate;

import com.dat.erp.constants.CalendarDayType;

import jakarta.validation.constraints.NotNull;

public record CalendarDateRequest(
        @NotNull LocalDate calDate,
        @NotNull CalendarDayType dayType,
        Boolean isPaidHoliday,
        String holidayName) {
}
