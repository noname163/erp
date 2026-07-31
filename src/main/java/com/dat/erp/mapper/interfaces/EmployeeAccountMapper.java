package com.dat.erp.mapper.interfaces;

import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.dat.erp.dto.request.CreateEmployeeRequest;
import com.dat.erp.dto.request.UserProfileCreateRequest;
import com.dat.erp.entities.Account;
import com.dat.erp.systemconfigs.CentralMapperConfig;

@Mapper(config = CentralMapperConfig.class, builder = @Builder(disableBuilder = true))
public interface EmployeeAccountMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "code", ignore = true)
    @Mapping(target = "companyCode", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "lastLogin", ignore = true)
    @Mapping(target = "numberToken", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "userProfile", ignore = true)
    @Mapping(target = "email", source = "email")
    Account toAccount(CreateEmployeeRequest request);

    @Mapping(target = "accountCode", source = "accountCode")
    @Mapping(target = "firstName", source = "request.firstName")
    @Mapping(target = "lastName", source = "request.lastName")
    @Mapping(target = "departmentCode", source = "request.departmentCode")
    UserProfileCreateRequest toUserProfileCreateRequest(CreateEmployeeRequest request, String accountCode);
}
