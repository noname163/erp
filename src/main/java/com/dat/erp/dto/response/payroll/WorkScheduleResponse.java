package com.dat.erp.dto.response.payroll;

import java.time.LocalDate;
import java.util.List;

import com.dat.erp.dto.request.payroll.WorkScheduleDetailRequest;

public record WorkScheduleResponse(
        String code,
        String name,
        String description,
        String scheduleType,
        Integer standardHoursPerDay,
        Integer standardMinutesPerDay,
        Boolean flexibleWorkingHours,
        Boolean crossMidnightAllowed,
        LocalDate effectiveFrom,
        LocalDate effectiveTo,
        List<WorkScheduleDetailRequest> details) {
}
