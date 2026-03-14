package com.dat.erp.services;

import com.dat.erp.dto.request.CreateEmployeeRequest;
import com.dat.erp.dto.request.EmployeeListRequest;
import com.dat.erp.dto.response.EmployeeResponse;
import com.dat.erp.dto.response.PaginationResponse;
import com.dat.erp.dto.response.employee.EmployeeListItem;

public interface EmployeeAccountService {
    EmployeeResponse createEmployee(CreateEmployeeRequest request);

    PaginationResponse<EmployeeListItem> getEmployees(EmployeeListRequest request);
}

