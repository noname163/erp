package com.dat.erp.services.payroll;

import java.time.LocalDate;

import com.dat.erp.constants.PayrollRerunMode;
import com.dat.erp.constants.PayrollStatus;
import com.dat.erp.dto.request.PayrollRerunRequest;
import com.dat.erp.dto.response.PagedResponse;
import com.dat.erp.dto.response.PayrollResultDetailResponse;
import com.dat.erp.dto.response.PayrollResultListResponse;
import com.dat.erp.dto.response.salary.MonthlySalaryCalculationResponse;
import com.dat.erp.entities.EmployeeSalary;
import com.dat.erp.entities.PayrollResult;
import com.dat.erp.entities.PayrollRun;

import java.util.List;
import java.util.Map;

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

    List<PayrollResult> resolveTargetResults(String payrollRunCode, String companyCode,
            PayrollRerunRequest request,
            PayrollRerunMode mode);

    String toResultJson(PayrollResult payrollResult);

    PayrollResult buildRerunPayrollResult(
            PayrollRun payrollRun,
            PayrollResult oldResult,
            EmployeeSalary activeSalary,
            MonthlySalaryCalculationResponse calculation,
            String companySecretKey);

    PayrollResult saveRerunPayRollResult(PayrollResult payrollResult);
    void softDeleteOldResult(String payrollRunCode);
    Map<String, PayrollResult> mapResultsByEmployeeCodeByPayRollResultCodes(List<String> payrollResultCodes);
}
