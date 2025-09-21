package com.dat.erp.mapper.interfaces;

import com.dat.erp.dto.request.RoleHasApiRequest;
import com.dat.erp.dto.response.RoleHasApiResponse;
import com.dat.erp.entities.RoleHasApi;
import com.dat.erp.systemconfigs.CentralMapperConfig;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class)
public interface RoleHasApiMapper {

    @Mapping(source = "roleId", target = "role.id")
    RoleHasApi toEntity(RoleHasApiRequest request);

    @Mapping(source = "role.name", target = "roleName")
    RoleHasApiResponse toResponse(RoleHasApi entity);
}
