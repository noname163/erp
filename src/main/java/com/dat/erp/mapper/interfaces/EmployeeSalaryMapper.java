package com.dat.erp.mapper.interfaces;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.dat.erp.dto.request.EmployeeSalaryRequest;
import com.dat.erp.dto.response.EmployeeSalaryResponse;
import com.dat.erp.entities.EmployeeSalary;
import com.dat.erp.systemconfigs.CentralMapperConfig;

@Mapper(config = CentralMapperConfig.class)
public interface EmployeeSalaryMapper {
    @Mapping(target = "userProfile", ignore = true)
    @Mapping(target = "salaryTemplate", ignore = true)
    @Mapping(target = "details", ignore = true)
    EmployeeSalary toEntity(EmployeeSalaryRequest request);

    @Mapping(target = "userProfileCode", source = "userProfile.code")
    EmployeeSalaryResponse toResponse(EmployeeSalary entity);
}

