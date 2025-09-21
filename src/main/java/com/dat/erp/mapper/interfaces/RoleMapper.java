package com.dat.erp.mapper.interfaces;

import com.dat.erp.dto.request.RoleRequest;
import com.dat.erp.dto.response.RoleResponse;
import com.dat.erp.entities.Role;
import com.dat.erp.systemconfigs.CentralMapperConfig;

import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class)
public interface RoleMapper {
    Role toEntity(RoleRequest request);

    RoleResponse toResponse(Role entity);
}
