package com.dat.erp.mapper.interfaces;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.dat.erp.dto.response.SalaryTemplateDetailListResponse;
import com.dat.erp.entities.SalaryTemplateDetail;
import com.dat.erp.systemconfigs.CentralMapperConfig;

@Mapper(config = CentralMapperConfig.class)
public interface SalaryTemplateDetailMapper {
    @Mapping(target = "salaryCode", source = "salary.code")
    @Mapping(target = "dependenceCode", source = "dependenceCode.code")
    @Mapping(target = "unitName", source = "unit.name")
    @Mapping(target = "salaryName", source = "salary.name")
    @Mapping(target = "calculateMethod", source = "salary.calculateMethod")
    @Mapping(target = "isDeduct", source = "salary.isDeduct")
    SalaryTemplateDetailListResponse toListResponse(SalaryTemplateDetail detail);

    List<SalaryTemplateDetailListResponse> toListResponses(List<SalaryTemplateDetail> details);
}
