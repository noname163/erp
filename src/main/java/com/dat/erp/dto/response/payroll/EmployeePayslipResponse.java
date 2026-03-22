package com.dat.erp.dto.response.payroll;

import java.util.List;

public record EmployeePayslipResponse(
        PayrollRunResponse run,
        PayrollEmployeeSummaryResponse summary,
        List<PayrollLineResponse> lines) {
}
