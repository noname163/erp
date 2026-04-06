package com.dat.erp.mapper.interfaces;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import com.dat.erp.constants.SalaryCalculateMethod;
import com.dat.erp.dto.request.SalaryRequest;
import com.dat.erp.dto.response.SalaryListResponse;
import com.dat.erp.dto.response.SalaryResponse;
import com.dat.erp.dto.response.SelectionOptionResponse;
import com.dat.erp.entities.Salary;
import com.dat.erp.systemconfigs.CentralMapperConfig;

@Mapper(config = CentralMapperConfig.class)
public interface SalaryMapper {
    Salary toEntity(SalaryRequest request);

    List<Salary> toEntities(List<SalaryRequest> requests);

    SalaryResponse toResponse(Salary entity);

    List<SalaryResponse> toResponses(List<Salary> entities);

    @Mapping(target = "formula", source = "calculateMethod")
    @Mapping(target = "calculateMethod", source = "calculateMethod")
    @Mapping(target = "isDeduct", source = "isDeduct", qualifiedByName = "toYesNo")
    SalaryListResponse toListResponse(Salary entity);

    List<SalaryListResponse> toListResponses(List<Salary> entities);

    SelectionOptionResponse toOptionResponse(Salary entity);

    List<SelectionOptionResponse> toOptionResponses(List<Salary> entities);

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

    @Named("toYesNo")
    default String toYesNo(Boolean value) {
        return Boolean.TRUE.equals(value) ? "Yes" : "No";
    }
}
