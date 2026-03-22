package com.dat.erp.dto.request.payroll;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CompanyCalendarRequest(
        @NotBlank String name,
        @NotNull LocalDate effectiveFrom,
        @NotNull LocalDate effectiveTo,
        @Valid List<CalendarDateRequest> dates) {
}
