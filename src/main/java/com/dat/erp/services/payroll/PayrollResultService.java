package com.dat.erp.services.payroll;

import java.time.LocalDate;

import com.dat.erp.constants.PayrollStatus;
import com.dat.erp.dto.response.PagedResponse;
import com.dat.erp.dto.response.PayrollResultListResponse;
import com.dat.erp.entities.PayrollRun;

public interface PayrollResultService {
    PagedResponse<PayrollResultListResponse> getPayrollResults(
            LocalDate createdDate,
            PayrollStatus sourceType,
            String employeeCode,
            Integer page,
            Integer size,
            String sortBy,
            String sortDir);

    void generatePayrollResult(PayrollRun payrollRun);
}
