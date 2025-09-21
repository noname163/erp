package com.dat.erp.mapper.interfaces;

import com.dat.erp.dto.request.EmployeeInformationRequest;
import com.dat.erp.dto.response.EmployeeInformationResponse;
import com.dat.erp.entities.EmployeeInformation;
import com.dat.erp.systemconfigs.CentralMapperConfig;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class)
public interface EmployeeMapper {

    @Mapping(source = "userCode", target = "user.code")
    @Mapping(source = "departmentCode", target = "department.code")
    @Mapping(source = "roleCode", target = "role.code")
    @Mapping(source = "companyCode", target = "company.code")
    EmployeeInformation toEntity(EmployeeInformationRequest request);

    @Mapping(source = "company.name", target = "companyName")
    @Mapping(source = "department.name", target = "departmentName")
    @Mapping(source = "role.name", target = "roleName")
    EmployeeInformationResponse toResponse(EmployeeInformation entity);
}
