package com.dat.erp.dto.response.salary;

import java.math.BigDecimal;

import com.dat.erp.constants.DayType;
import com.dat.erp.constants.SalaryCalculateMethod;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MonthlySalaryDetailAuditResponse {
    private String salaryCode;
    private DayType dayType;
    private SalaryCalculateMethod calculateMethod;
    private String dependenceCode;
    private String unit;
    private BigDecimal baseAmount;
    private BigDecimal configuredAmount;
    private BigDecimal dependencyAmount;
    private BigDecimal result;
    private BigDecimal totalAmount;
}
