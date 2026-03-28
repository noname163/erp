package com.dat.erp.dto.response;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeePayrollPolicyResponse {
    private String code;
    private String userProfileCode;
    private String payrollPolicyCode;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private Boolean isActive;
}
