package com.dat.erp.mapper.interfaces;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.mapstruct.Mapper;

import com.dat.erp.dto.request.SalaryTemplateRequest;
import com.dat.erp.dto.response.SalaryTemplateListResponse;
import com.dat.erp.dto.response.SalaryTemplateResponse;
import com.dat.erp.entities.SalaryTemplate;
import com.dat.erp.systemconfigs.CentralMapperConfig;

@Mapper(config = CentralMapperConfig.class)
public interface SalaryTemplateMapper {
    SalaryTemplate toEntity(SalaryTemplateRequest request);

    SalaryTemplateResponse toResponse(SalaryTemplate entity);

    SalaryTemplateListResponse toListResponse(SalaryTemplate entity);

    default BigDecimal map(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    default String map(BigDecimal value) {
        return value == null ? null : value.toPlainString();
    }

    default String map(LocalDate value) {
        return value == null ? null : value.toString();
    }
}

