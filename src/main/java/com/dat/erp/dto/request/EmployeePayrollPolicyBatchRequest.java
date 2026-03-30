package com.dat.erp.dto.request;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonAlias;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EmployeePayrollPolicyBatchRequest {
    @JsonAlias("payrollPolicyCode")
    @NotBlank(message = "policyCode is required")
    private String policyCode;

    @JsonAlias("userProfileCodes")
    @NotEmpty(message = "employeeCodes is required")
    private List<@NotBlank(message = "employeeCodes must not contain blank values") String> employeeCodes;

    @NotNull(message = "effectiveFrom is required")
    private LocalDate effectiveFrom;

    @NotNull(message = "effectiveTo is required")
    private LocalDate effectiveTo;
}
