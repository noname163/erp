package com.dat.erp.mapper.interfaces;

import java.util.List;

import org.mapstruct.Mapper;

import com.dat.erp.constants.SalaryCalculateMethod;
import com.dat.erp.dto.request.SalaryRequest;
import com.dat.erp.dto.response.SalaryResponse;
import com.dat.erp.entities.Salary;
import com.dat.erp.systemconfigs.CentralMapperConfig;

@Mapper(config = CentralMapperConfig.class)
public interface SalaryMapper {
    Salary toEntity(SalaryRequest request);

    List<Salary> toEntities(List<SalaryRequest> requests);

    SalaryResponse toResponse(Salary entity);

    List<SalaryResponse> toResponses(List<Salary> entities);

    default SalaryCalculateMethod mapCalculateMethod(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return SalaryCalculateMethod.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    default String mapCalculateMethod(SalaryCalculateMethod value) {
        return value == null ? null : value.name();
    }
}

