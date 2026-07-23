package com.dat.erp.mapper.interfaces;

import java.util.List;

import org.mapstruct.Mapper;

import com.dat.erp.dto.response.SelectionOptionResponse;
import com.dat.erp.entities.Role;
import com.dat.erp.systemconfigs.CentralMapperConfig;

@Mapper(config = CentralMapperConfig.class)
public interface RoleMapper {
    SelectionOptionResponse toOptionResponse(Role role);

    List<SelectionOptionResponse> toOptionResponses(List<Role> roles);
}
