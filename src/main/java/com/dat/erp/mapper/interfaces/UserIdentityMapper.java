package com.dat.erp.mapper.interfaces;

import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.dat.erp.dto.request.CreateEmployeeRequest;
import com.dat.erp.entities.UserIdentity;
import com.dat.erp.systemconfigs.CentralMapperConfig;

@Mapper(config = CentralMapperConfig.class, builder = @Builder(disableBuilder = true))
public interface UserIdentityMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "code", ignore = true)
    @Mapping(target = "companyCode", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "userProfile", ignore = true)
    @Mapping(target = "idType", constant = "GENDER")
    @Mapping(target = "idValue", source = "gender")
    @Mapping(target = "issuedDate", ignore = true)
    @Mapping(target = "expiryDate", ignore = true)
    UserIdentity toGenderIdentity(CreateEmployeeRequest request);
}
