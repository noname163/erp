package com.dat.erp.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class SalaryTemplateDetailRequest {
    @NotBlank(message = "salaryCode is required")
    private String salaryCode;

    @NotBlank(message = "amount is required")
    @Pattern(regexp = "^[0-9]+(\\.[0-9]+)?$", message = "amount must be a positive numeric value")
    private String amount;

    @NotBlank(message = "quantity is required")
    @Pattern(regexp = "^[0-9]+$", message = "quantity must be a positive integer")
    private String quantity;

    @NotBlank(message = "unitCode is required")
    private String unitCode;

    @NotBlank(message = "sequenceOrder is required")
    @Pattern(regexp = "^[0-9]+$", message = "sequenceOrder must be a positive integer")
    private String sequenceOrder;
}

