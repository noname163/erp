package com.dat.erp.dto.response.payroll;

import java.util.List;

public record PayrollRunDetailResponse(
        PayrollRunResponse run,
        List<PayrollEmployeeSummaryResponse> employees) {
}
