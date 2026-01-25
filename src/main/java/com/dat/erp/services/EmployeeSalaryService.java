package com.dat.erp.services;

import com.dat.erp.dto.request.EmployeeSalaryRequest;
import com.dat.erp.dto.response.EmployeeSalaryResponse;

public interface EmployeeSalaryService {
    EmployeeSalaryResponse createEmployeeSalary(EmployeeSalaryRequest request);
}

