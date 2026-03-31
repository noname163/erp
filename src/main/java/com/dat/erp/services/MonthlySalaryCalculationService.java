package com.dat.erp.services;

import java.time.YearMonth;

import com.dat.erp.dto.response.salary.MonthlySalaryCalculationResponse;

public interface MonthlySalaryCalculationService {
    MonthlySalaryCalculationResponse calculateEmployeeMonthlySalary(String employeeCode, YearMonth month);
}
