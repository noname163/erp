package com.dat.erp.dto.request.payroll;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.constraints.NotNull;

public record PayrollRunPreviewRequest(
        @NotNull LocalDate periodStart,
        @NotNull LocalDate periodEnd,
        List<String> userProfileCodes) {
}
