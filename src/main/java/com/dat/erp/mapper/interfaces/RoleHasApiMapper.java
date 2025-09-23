package com.dat.erp.mapper.interfaces;

import com.dat.erp.dto.request.RoleHasApiRequest;
import com.dat.erp.dto.response.RoleHasApiResponse;
import com.dat.erp.entities.RoleHasApi;
import com.dat.erp.systemconfigs.CentralMapperConfig;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class)
public interface RoleHasApiMapper {

    // Request → Entity
    @Mapping(target = "endpoints", source = "endpoints")
    RoleHasApi toEntity(RoleHasApiRequest request);

    // Entity → Response
    @Mapping(source = "role.name", target = "roleName")
    @Mapping(target = "endpoints", source = "endpoints")
    RoleHasApiResponse toResponse(RoleHasApi entity);

    // Support methods for MapStruct
    default String map(List<String> endpoints) {
        return endpoints == null ? null : String.join(",", endpoints);
    }

    default List<String> map(String endpoints) {
        return endpoints == null || endpoints.isBlank()
                ? List.of()
                : List.of(endpoints.split(","));
    }
}
