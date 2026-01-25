package com.dat.erp.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class EmployeeSalaryRequest {
    @NotBlank(message = "userProfileCode is required")
    @Size(max = 50, message = "userProfileCode must be at most 50 characters")
    private String userProfileCode;

    @NotNull(message = "effectiveFrom is required")
    private LocalDate effectiveFrom;

    @NotNull(message = "effectiveTo is required")
    private LocalDate effectiveTo;

    @NotBlank(message = "totalAmount is required")
    @Pattern(regexp = "^[0-9]+(\\.[0-9]+)?$", message = "totalAmount must be a positive numeric value")
    private String totalAmount;

    @NotBlank(message = "currency is required")
    @Size(max = 10, message = "currency must be at most 10 characters")
    private String currency;
}

