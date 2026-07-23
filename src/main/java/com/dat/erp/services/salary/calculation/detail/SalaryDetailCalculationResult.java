package com.dat.erp.services.salary.calculation.detail;

import java.math.BigDecimal;

import com.dat.erp.constants.DayType;
import com.dat.erp.constants.SalaryCalculateMethod;

public record SalaryDetailCalculationResult(String salaryCode, DayType dayType, SalaryCalculateMethod calculateMethod,
        String dependenceCode, BigDecimal baseAmount, BigDecimal configuredAmount, BigDecimal dependencyAmount,
        BigDecimal result) {
}
