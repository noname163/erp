package com.dat.erp.mapper.interfaces;

import com.dat.erp.dto.request.CompanyRequest;
import com.dat.erp.dto.response.CompanyResponse;
import com.dat.erp.entities.Company;
import com.dat.erp.systemconfigs.CentralMapperConfig;

import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class)
public interface CompanyMapper {
    Company toEntity(CompanyRequest request);

    CompanyResponse toResponse(Company entity);
}
