package com.dat.erp.dto.request.payroll;

import java.time.LocalTime;

import jakarta.validation.constraints.NotNull;

public record WorkScheduleDetailRequest(
        @NotNull Integer dayOfWeek,
        @NotNull Boolean isWorkingDay,
        LocalTime startTime,
        LocalTime endTime,
        Integer breakMinutes,
        Boolean paidBreak,
        Integer fullDayThresholdMinutes) {
}
