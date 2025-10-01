package com.dat.erp.dto.response;

import lombok.Data;

@Data
public class EmployeeInformationDetailResponse extends EmployeeInformationResponse {
    private CompanyResponse company;
    private DepartmentResponse department;
    private RoleResponse role;
}
