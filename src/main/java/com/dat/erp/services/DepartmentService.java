package com.dat.erp.services;

import com.dat.erp.dto.request.DepartmentRequest;
import com.dat.erp.dto.response.DepartmentResponse;
import com.dat.erp.dto.response.PagedResponse;

public interface DepartmentService {
    public String createDepartment(DepartmentRequest departmentRequest);

    public PagedResponse<DepartmentResponse> getDepartmentByCompanyCode(String searchKey, String searchValue,
            Integer page, Integer size, String sortBy, String sortDir);
}
