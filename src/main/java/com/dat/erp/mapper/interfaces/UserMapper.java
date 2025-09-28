package com.dat.erp.mapper.interfaces;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.dat.erp.dto.request.UserInformationRequest;
import com.dat.erp.dto.response.UserInformationResponse;
import com.dat.erp.entities.UserInformation;
import com.dat.erp.systemconfigs.CentralMapperConfig;

@Mapper(config = CentralMapperConfig.class)
public interface UserMapper {

    UserInformation toEntity(UserInformationRequest request);

    @Mapping(source = "code", target = "code")
    UserInformationResponse toResponse(UserInformation entity);
}