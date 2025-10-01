package com.dat.erp.dto.response;

import java.util.List;

import lombok.Data;

@Data
public class CompanyDetailResponse extends CompanyResponse {
    List<DepartmentResponse> department;
    List<EmployeeInformationResponse> employees;
    List<RoleResponse> roles;
}
