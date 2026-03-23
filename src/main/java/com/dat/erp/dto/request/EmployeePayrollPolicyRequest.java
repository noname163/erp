package com.dat.erp.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EmployeePayrollPolicyRequest {
    @NotBlank(message = "userProfileCode is required")
    private String userProfileCode;

    @NotBlank(message = "payrollPolicyCode is required")
    private String payrollPolicyCode;

    @NotNull(message = "effectiveFrom is required")
    private LocalDate effectiveFrom;

    @NotNull(message = "effectiveTo is required")
    private LocalDate effectiveTo;
}
