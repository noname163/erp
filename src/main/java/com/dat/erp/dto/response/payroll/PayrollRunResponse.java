package com.dat.erp.dto.response.payroll;

import java.time.LocalDate;

import com.dat.erp.constants.PayrollRunStatus;

public record PayrollRunResponse(
        String code,
        String period,
        LocalDate periodStart,
        LocalDate periodEnd,
        Boolean isPreview,
        PayrollRunStatus status,
        String snapshotVersion,
        String replayedFromRunCode,
        Integer warningCount,
        Integer errorCount) {
}
