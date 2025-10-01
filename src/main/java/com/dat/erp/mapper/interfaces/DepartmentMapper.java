package com.dat.erp.mapper.interfaces;

import com.dat.erp.dto.request.DepartmentRequest;
import com.dat.erp.dto.response.DepartmentResponse;
import com.dat.erp.entities.Department;
import com.dat.erp.systemconfigs.CentralMapperConfig;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class)
public interface DepartmentMapper {
    Department toEntity(DepartmentRequest request);

    @Mapping(source = "company.name", target = "companyName")
    @Mapping(source = "company.code", target = "companyCode")
    DepartmentResponse toResponse(Department entity);
}
