package com.dat.erp.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EmployeeInformationRequest {

    @NotBlank(message = "User code is required")
    private String userCode;

    private String nickname;

    @NotBlank(message = "Department code is required")
    private String departmentCode;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;

    private String jobTitle;

    @NotNull(message = "Hire date is required")
    private LocalDate hireDate;

    @NotBlank(message = "Role code is required")
    private String roleCode;

    @NotBlank(message = "Company code is required")
    private String companyCode;
}
