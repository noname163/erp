package com.dat.erp.services;

import java.util.List;

import com.dat.erp.dto.request.EmployeeSalaryDetailRequest;

public interface EmployeeSalaryDetailService {
    String createEmployeeSalaryDetails(List<EmployeeSalaryDetailRequest> requests, String employeeSalaryCode);
}
