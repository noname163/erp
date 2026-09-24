package com.dat.erp.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateEmployeeRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 255, message = "Email must be at most 255 characters")
    private String email;

    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name must be at most 100 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name must be at most 100 characters")
    private String lastName;

    @NotBlank(message = "Department code is required")
    @Size(max = 50, message = "Department code must be at most 50 characters")
    private String departmentCode;

    @NotBlank(message = "Role code is required")
    @Size(max = 100, message = "Role code must be at most 100 characters")
    private String roleCode;

    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^\\+?[0-9\\- ]{7,20}$", message = "Invalid phone number format")
    private String phone;
}
