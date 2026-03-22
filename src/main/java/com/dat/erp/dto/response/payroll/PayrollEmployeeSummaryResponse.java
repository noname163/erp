package com.dat.erp.dto.response.payroll;

import com.dat.erp.constants.PayrollSummaryStatus;

public record PayrollEmployeeSummaryResponse(
        String employeeCode,
        String employeeName,
        String grossAmount,
        String deductionAmount,
        String netAmount,
        String currency,
        PayrollSummaryStatus status,
        Boolean hasBlockingIssue,
        String issueMessage) {
}
