package com.dat.erp.services.payroll;

import java.time.LocalDateTime;
import java.time.YearMonth;

import com.dat.erp.constants.PayrollRunStatus;
import com.dat.erp.dto.request.PayrollRerunRequest;
import com.dat.erp.dto.response.PagedResponse;
import com.dat.erp.dto.response.PayrollRerunResponse;
import com.dat.erp.dto.response.PayrollRunResponse;

public interface PayrollRunService {
    PagedResponse<PayrollRunResponse> getPayrollRuns(
            PayrollRunStatus status,
            LocalDateTime runAtFrom,
            LocalDateTime runAtTo,
            LocalDateTime closeAtFrom,
            LocalDateTime closeAtTo,
            Integer page,
            Integer size,
            String sortBy,
            String sortDir);

    PayrollRunResponse runPayroll(YearMonth runDate);

    PayrollRerunResponse rerunPayroll(String payrollRunCode, PayrollRerunRequest request);

}
