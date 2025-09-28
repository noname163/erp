package com.dat.erp.dto.response;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.dat.erp.constants.CommonStatus;

@Data
public class EmployeeInformationResponse {

    private String code;
    private String nickname;
    private String email;
    private String jobTitle;
    private CommonStatus employmentStatus;
    private LocalDate hireDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String roleName;
    private String departmentName;
    private String companyName;
}
