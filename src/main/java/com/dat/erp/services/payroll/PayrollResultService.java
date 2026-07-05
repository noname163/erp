package com.dat.erp.services.payroll;

import java.time.LocalDate;

import com.dat.erp.constants.PayrollStatus;
import com.dat.erp.dto.response.PagedResponse;
import com.dat.erp.dto.response.PayrollResultDetailResponse;
import com.dat.erp.dto.response.PayrollResultListResponse;
import com.dat.erp.entities.PayrollRun;

import java.util.List;

public interface PayrollResultService {
    PagedResponse<PayrollResultListResponse> getPayrollResults(
            String payrollRunCode,
            LocalDate createdDate,
            PayrollStatus sourceType,
            String employeeCode,
            Integer page,
            Integer size,
            String sortBy,
            String sortDir);

    void generatePayrollResult(PayrollRun payrollRun);

    List<PayrollResultDetailResponse> getPayrollResultDetails(String payrollResultCode);
}
