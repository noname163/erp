package com.dat.erp.mapper.interfaces;

import org.mapstruct.Mapper;

import com.dat.erp.dto.request.SystemApiRequest;
import com.dat.erp.dto.response.SystemApiResponse;
import com.dat.erp.entities.SystemApi;
import com.dat.erp.systemconfigs.CentralMapperConfig;

@Mapper(config = CentralMapperConfig.class)
public interface SystemApiMapper {

    SystemApi toEntity(SystemApiRequest request);

    SystemApiResponse toResponse(SystemApi entity);
}
