package com.dat.erp.mapper.interfaces;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.dat.erp.dto.request.RoleRequest;
import com.dat.erp.dto.response.RoleResponse;
import com.dat.erp.entities.Role;
import com.dat.erp.systemconfigs.CentralMapperConfig;

@Mapper(config = CentralMapperConfig.class)
public interface RoleMapper {

    Role toEntity(RoleRequest request);

    RoleResponse toResponse(Role entity);

    List<RoleResponse> toResponseList(List<Role> entities);

    List<Role> toEntityList(List<RoleRequest> requests);

}
