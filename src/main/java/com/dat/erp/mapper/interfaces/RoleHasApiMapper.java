package com.dat.erp.mapper.interfaces;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.dat.erp.dto.request.RoleHasApiRequest;
import com.dat.erp.dto.response.RoleHasApiResponse;
import com.dat.erp.entities.RoleHasApi;
import com.dat.erp.systemconfigs.CentralMapperConfig;
import com.dat.erp.utils.PermissionUtils;

@Mapper(config = CentralMapperConfig.class)
public interface RoleHasApiMapper {

    // Request → Entity

    @Mapping(target = "permission", expression = "java(com.dat.erp.utils.PermissionUtils.toInt(request))")
    RoleHasApi toEntity(RoleHasApiRequest request);

    // Entity → Response
    @Mapping(target = "create", ignore = true) // will be set in @AfterMapping
    @Mapping(target = "read", ignore = true)
    @Mapping(target = "update", ignore = true)
    @Mapping(target = "delete", ignore = true)
    @Mapping(source = "role.name", target = "roleName")
    RoleHasApiResponse toResponse(RoleHasApi entity);

    @AfterMapping
    default void setPermission(RoleHasApi entity, @MappingTarget RoleHasApiResponse response) {
        PermissionUtils.fromInt(response, entity.getPermission());
    }
}
