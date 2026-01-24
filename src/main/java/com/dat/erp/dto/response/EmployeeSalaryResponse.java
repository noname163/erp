package com.dat.erp.dto.response;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeSalaryResponse {
    private String code;
    private String userProfileCode;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private String totalAmount;
    private String currency;
}

