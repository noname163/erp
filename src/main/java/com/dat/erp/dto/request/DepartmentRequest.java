package com.dat.erp.dto.request;

import com.dat.erp.constants.CommonStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DepartmentRequest {
    @NotBlank(message = "Department name is required")
    @Size(max = 100, message = "Department name must be at most 100 characters")
    private String name;

    @Size(max = 255, message = "Description must be at most 255 characters")
    private String description;

    @NotBlank(message = "Status is required")
    @Pattern(regexp = "^(Active|Inactive)$", message = "Status must be 'Active' or 'Inactive'")
    private CommonStatus status;

    @NotBlank(message = "Company code is required")
    @Size(max = 50, message = "Company code must be at most 50 characters")
    private String companyCode;
}
