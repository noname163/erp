package com.dat.erp.services;

import com.dat.erp.dto.request.EmployeeListRequest;
import com.dat.erp.dto.response.PaginationResponse;
import com.dat.erp.dto.response.employee.EmployeeListItem;

public interface EmployeeListService {
    PaginationResponse<EmployeeListItem> getEmployees(EmployeeListRequest request);
}

