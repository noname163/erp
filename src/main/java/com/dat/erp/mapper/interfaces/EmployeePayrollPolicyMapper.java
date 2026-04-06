package com.dat.erp.mapper.interfaces;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.dat.erp.dto.response.EmployeePayrollPolicyResponse;
import com.dat.erp.entities.EmployeePayrollPolicy;
import com.dat.erp.systemconfigs.CentralMapperConfig;

@Mapper(config = CentralMapperConfig.class)
public interface EmployeePayrollPolicyMapper {
    @Mapping(target = "userProfileCode", source = "userProfile.code")
    @Mapping(target = "payrollPolicyCode", source = "payrollPolicy.code")
    EmployeePayrollPolicyResponse toResponse(EmployeePayrollPolicy employeePayrollPolicy);

    List<EmployeePayrollPolicyResponse> toResponses(List<EmployeePayrollPolicy> employeePayrollPolicies);
}
