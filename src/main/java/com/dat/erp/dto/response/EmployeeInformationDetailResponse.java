package com.dat.erp.dto.response;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class EmployeeInformationDetailResponse extends EmployeeInformationResponse {
    private CompanyResponse company;
    private DepartmentResponse department;
    private RoleResponse role;
}
