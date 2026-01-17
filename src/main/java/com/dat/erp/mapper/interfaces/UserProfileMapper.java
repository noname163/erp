package com.dat.erp.mapper.interfaces;

import org.mapstruct.AfterMapping;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.dat.erp.dto.request.UserProfileCreateRequest;
import com.dat.erp.dto.response.EmployeeResponse;
import com.dat.erp.entities.UserProfile;
import com.dat.erp.systemconfigs.CentralMapperConfig;

@Mapper(config = CentralMapperConfig.class, builder = @Builder(disableBuilder = true))
public interface UserProfileMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "code", ignore = true)
    @Mapping(target = "companyCode", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "account", ignore = true)
    @Mapping(target = "department", ignore = true)
    @Mapping(target = "jobTitle", ignore = true)
    @Mapping(target = "managerCode", ignore = true)
    @Mapping(target = "hireDate", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "identities", ignore = true)
    @Mapping(target = "skills", ignore = true)
    @Mapping(target = "employeeSalaries", ignore = true)
    @Mapping(target = "ptos", ignore = true)
    @Mapping(target = "dailyWorks", ignore = true)
    @Mapping(target = "phoneNumber", ignore = true)
    UserProfile toUserProfile(UserProfileCreateRequest request);

    @Mapping(target = "code", source = "code")
    @Mapping(target = "email", source = "account.email")
    @Mapping(target = "department", source = "department.name")
    @Mapping(target = "role", source = "account.role.name")
    @Mapping(target = "fullName", ignore = true)
    EmployeeResponse toEmployeeResponse(UserProfile profile);

    @AfterMapping
    default void fillFullName(UserProfile profile, @MappingTarget EmployeeResponse response) {
        response.setFullName(buildFullName(profile.getFirstName(), profile.getLastName()));
    }

    default String buildFullName(String firstName, String lastName) {
        String fn = firstName == null ? "" : firstName.trim();
        String ln = lastName == null ? "" : lastName.trim();
        return (fn + " " + ln).trim();
    }
}
