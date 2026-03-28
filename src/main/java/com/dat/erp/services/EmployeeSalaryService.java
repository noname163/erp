package com.dat.erp.services;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.dat.erp.dto.request.EmployeeSalaryRequest;
import com.dat.erp.dto.response.EmployeeSalaryListResponse;
import com.dat.erp.dto.response.EmployeeSalaryResponse;
import com.dat.erp.dto.response.PagedResponse;

public interface EmployeeSalaryService {
    EmployeeSalaryResponse createEmployeeSalary(EmployeeSalaryRequest request);

    PagedResponse<EmployeeSalaryListResponse> getEmployeeSalaries(String employeeName, BigDecimal minAmount,
            BigDecimal maxAmount, LocalDate effectiveFrom, LocalDate effectiveTo, Integer page, Integer size,
            String sortBy, String sortDir);
}
