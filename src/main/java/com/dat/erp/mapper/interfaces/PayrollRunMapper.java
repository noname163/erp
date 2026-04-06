package com.dat.erp.mapper.interfaces;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import com.dat.erp.dto.response.PayrollRunResponse;
import com.dat.erp.entities.PayrollRun;
import com.dat.erp.systemconfigs.CentralMapperConfig;

@Mapper(config = CentralMapperConfig.class)
public interface PayrollRunMapper {
    @Mapping(target = "closeAt", source = "closedAt")
    @Mapping(target = "runBy", source = "createdBy", qualifiedByName = "trimToNull")
    @Mapping(target = "updatedBy", source = "updatedBy", qualifiedByName = "trimToNull")
    PayrollRunResponse toResponse(PayrollRun payrollRun);

    @Named("trimToNull")
    default String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isBlank() ? null : trimmed;
    }
}
