package com.dat.erp.dto.response;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CompanyDetailResponse extends CompanyResponse {
    List<DepartmentResponse> department;
    List<EmployeeInformationResponse> employees;
    List<RoleResponse> roles;
}
