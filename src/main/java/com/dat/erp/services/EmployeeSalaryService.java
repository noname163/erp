package com.dat.erp.services;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.dat.erp.dto.request.EmployeeSalaryRequest;
import com.dat.erp.dto.response.EmployeeSalaryListResponse;
import com.dat.erp.dto.response.EmployeeSalaryResponse;
import com.dat.erp.dto.response.PagedResponse;
import com.dat.erp.entities.EmployeeSalary;
import com.dat.erp.entities.PayrollResult;

public interface EmployeeSalaryService {
    EmployeeSalaryResponse createEmployeeSalary(EmployeeSalaryRequest request);

    PagedResponse<EmployeeSalaryListResponse> getEmployeeSalaries(String employeeName, BigDecimal minAmount,
            BigDecimal maxAmount, LocalDate effectiveFrom, LocalDate effectiveTo, Integer page, Integer size,
            String sortBy, String sortDir);

    void employeeSalaryCalculation(String companyCode, List<String> employeeCodes, List<PayrollResult> payrollResults,
            LocalDate runDate);

    EmployeeSalary getActiveByEmployeeCodeAndDate(String employeeCode, LocalDate runDate);
}
