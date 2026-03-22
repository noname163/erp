package com.dat.erp.dto.request.payroll;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record WorkScheduleRequest(
        @NotBlank String name,
        String description,
        String scheduleType,
        Integer standardHoursPerDay,
        Integer standardMinutesPerDay,
        Boolean flexibleWorkingHours,
        Boolean crossMidnightAllowed,
        @NotNull LocalDate effectiveFrom,
        @NotNull LocalDate effectiveTo,
        @Valid List<WorkScheduleDetailRequest> details) {
}
