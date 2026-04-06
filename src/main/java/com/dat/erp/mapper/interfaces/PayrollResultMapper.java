package com.dat.erp.mapper.interfaces;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import com.dat.erp.dto.response.PayrollResultListResponse;
import com.dat.erp.repositories.projections.PayrollResultListProjection;
import com.dat.erp.systemconfigs.CentralMapperConfig;
import com.dat.erp.utils.CompanySecretKeyCryptoUtils;

@Mapper(config = CentralMapperConfig.class)
public interface PayrollResultMapper {
    @Mapping(target = "payrollRunCode", source = "payrollRunCode", qualifiedByName = "trimToNull")
    @Mapping(target = "salaryName", source = "salaryName", qualifiedByName = "trimToNull")
    @Mapping(target = "expectedAmount", source = "expectedAmount", qualifiedByName = "decryptAmount")
    @Mapping(target = "employeeName", source = "employeeName", qualifiedByName = "trimToNull")
    @Mapping(target = "actualAmount", source = "actualAmount", qualifiedByName = "decryptAmount")
    @Mapping(target = "currency", source = "currency", qualifiedByName = "trimToNull")
    @Mapping(target = "unitName", source = "unitName", qualifiedByName = "trimToNull")
    @Mapping(target = "retroReason", source = "retroReason", qualifiedByName = "trimToNull")
    @Mapping(target = "period", source = "period", qualifiedByName = "trimToNull")
    @Mapping(target = "employeeCode", source = "employeeCode", qualifiedByName = "trimToNull")
    PayrollResultListResponse toListResponse(PayrollResultListProjection projection, @Context String companySecretKey);

    @Named("decryptAmount")
    default String decryptAmount(String value, @Context String companySecretKey) {
        return CompanySecretKeyCryptoUtils.decrypt(value, companySecretKey);
    }

    @Named("trimToNull")
    default String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isBlank() ? null : trimmed;
    }
}
