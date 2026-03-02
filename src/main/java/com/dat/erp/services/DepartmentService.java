package com.dat.erp.services;

import java.util.List;

import com.dat.erp.dto.request.DepartmentRequest;
import com.dat.erp.dto.response.PagedResponse;
import com.dat.erp.dto.response.SelectionOptionResponse;
import com.dat.erp.dto.response.department.DepartmentResponse;

public interface DepartmentService {
    public String createDepartment(DepartmentRequest departmentRequest);

    public String createDefaultDepartment(DepartmentRequest departmentRequest, String actorCode);

    public PagedResponse<DepartmentResponse> getDepartmentByCompanyCode(String searchKey, String searchValue,
            Integer page, Integer size, String sortBy, String sortDir);

    public List<SelectionOptionResponse> getDepartmentOptionsByCompanyCode(String name);
}
