package com.dat.erp.data;

import com.dat.erp.entities.UserProfile;
import com.dat.erp.utils.ErrorUtils;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EmployeeOwnedRecordData {

    private final UserProfile userProfile;
    private final String employeeCode;

    public EmployeeOwnedRecordData(
            UserProfile userProfile,
            String employeeCode) {

        this.userProfile = userProfile;
        this.employeeCode = employeeCode;

        if (userProfile == null && (employeeCode == null || employeeCode.isBlank())) {
            ErrorUtils.requireNonNull(userProfile, "Employee owner is required");
        }
    }
}
