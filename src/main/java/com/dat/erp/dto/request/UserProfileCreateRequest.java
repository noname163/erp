package com.dat.erp.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserProfileCreateRequest {
    @NotBlank(message = "Account code is required")
    @Size(max = 50, message = "Account code must be at most 50 characters")
    private String accountCode;

    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name must be at most 100 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name must be at most 100 characters")
    private String lastName;

    @NotBlank(message = "Department code is required")
    @Size(max = 50, message = "Department code must be at most 50 characters")
    private String departmentCode;
}

