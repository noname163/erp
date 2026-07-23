package com.dat.erp.mapper.interfaces;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.dat.erp.dto.response.PayrollPolicyResponse;
import com.dat.erp.entities.PayrollPolicy;
import com.dat.erp.systemconfigs.CentralMapperConfig;

@Mapper(config = CentralMapperConfig.class)
public interface PayrollPolicyMapper {
    @Mapping(target = "unitCode", source = "unit.code")
    PayrollPolicyResponse toResponse(PayrollPolicy payrollPolicy);

    List<PayrollPolicyResponse> toResponses(List<PayrollPolicy> payrollPolicies);
}
