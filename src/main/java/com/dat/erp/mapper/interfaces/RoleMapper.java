package com.dat.erp.mapper.interfaces;

import com.dat.erp.dto.request.RoleRequest;
import com.dat.erp.dto.response.RoleResponse;
import com.dat.erp.entities.Role;
import com.dat.erp.systemconfigs.CentralMapperConfig;
import com.dat.erp.utils.PermissionUtils;

import java.util.List;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = CentralMapperConfig.class)
public interface RoleMapper {
    @Mapping(target = "permission", expression = "java(com.dat.erp.utils.PermissionUtils.toInt(request))")
    Role toEntity(RoleRequest request);

    @Mapping(target = "create", ignore = true) // will be set in @AfterMapping
    @Mapping(target = "read", ignore = true)
    @Mapping(target = "update", ignore = true)
    @Mapping(target = "delete", ignore = true)
    RoleResponse toResponse(Role entity);

    List<RoleResponse> toResponseList(List<Role> entities);

    List<Role> toEntityList(List<RoleRequest> requests);

    @AfterMapping
    default void setPermission(Role entity, @MappingTarget RoleResponse response) {
        PermissionUtils.fromInt(response, entity.getPermission());
    }
}
