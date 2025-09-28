package com.dat.erp.mapper.interfaces;

import com.dat.erp.dto.request.EmployeeInformationRequest;
import com.dat.erp.dto.response.EmployeeInformationResponse;
import com.dat.erp.entities.EmployeeInformation;
import com.dat.erp.systemconfigs.CentralMapperConfig;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class)
public interface EmployeeMapper {

    @Mapping(target = "company", ignore = true) // will be set in service layer
    @Mapping(target = "department", ignore = true) // will be set in service layer
    @Mapping(target = "role", ignore = true) // will be set in service layer
    EmployeeInformation toEntity(EmployeeInformationRequest request);

    @Mapping(source = "company.name", target = "companyName")
    @Mapping(source = "department.name", target = "departmentName")
    @Mapping(source = "role.name", target = "roleName")
    EmployeeInformationResponse toResponse(EmployeeInformation entity);
}