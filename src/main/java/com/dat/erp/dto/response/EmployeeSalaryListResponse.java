package com.dat.erp.dto.response;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeSalaryListResponse {
    private String salaryCode;
    private String employeeName;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private String totalAmount;
    private String currency;
}
