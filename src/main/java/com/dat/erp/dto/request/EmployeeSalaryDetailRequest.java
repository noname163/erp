package com.dat.erp.dto.request;

import com.dat.erp.constants.DailyWorkWorkType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class EmployeeSalaryDetailRequest {
    @NotBlank(message = "salaryCode is required")
    private String salaryCode;

    @NotBlank(message = "employeeSalaryCode is required")
    private String employeeSalaryCode;

    @NotBlank(message = "amount is required")
    @Pattern(regexp = "^[0-9]+(\\.[0-9]+)?$", message = "amount must be a positive numeric value")
    private String amount;

    private String dependenceCode;

    @NotNull(message = "dailyWorkWorkType is required")
    private DailyWorkWorkType dailyWorkWorkType;
}
