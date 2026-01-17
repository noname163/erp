package com.dat.erp.services;

import com.dat.erp.dto.request.CreateEmployeeRequest;
import com.dat.erp.dto.response.EmployeeResponse;

public interface EmployeeAccountService {
    EmployeeResponse createEmployee(CreateEmployeeRequest request);
}

