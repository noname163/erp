package com.dat.erp.services;

import java.util.List;

import com.dat.erp.dto.request.DepartmentRequest;
import com.dat.erp.dto.response.DepartmentResponse;

public interface DepartmentService {
    public String createDepartment(DepartmentRequest departmentRequest);

    public List<DepartmentResponse> getDepartmentByCompanyCode(String companyCode, Integer page, Integer size);
}
