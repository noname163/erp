package com.dat.erp.dto.response;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class EmployeeInformationResponse {

    private String code;
    private String nickname;
    private String email;
    private String jobTitle;
    private String employmentStatus;
    private LocalDate hireDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String roleName;
    private String departmentName;
    private String companyName;
}
