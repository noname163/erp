package com.dat.erp.mapper.interfaces;

import com.dat.erp.dto.request.DepartmentRequest;
import com.dat.erp.dto.response.DepartmentResponse;
import com.dat.erp.entities.Department;
import com.dat.erp.systemconfigs.CentralMapperConfig;

import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class)
public interface DepartmentMapper {
    Department toEntity(DepartmentRequest request);

    DepartmentResponse toResponse(Department entity);
}
